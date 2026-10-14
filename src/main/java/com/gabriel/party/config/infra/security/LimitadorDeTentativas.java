package com.gabriel.party.config.infra.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Conta tentativas (de login, de recuperação de senha...) dentro de uma janela de tempo.
 * Usa o Redis, que é o mesmo para todas as cópias da aplicação. Se o Redis cair, continua
 * contando na memória desta cópia (plano B): protege menos, mas não para o login.
 */
@Component
public class LimitadorDeTentativas {

    private static final Logger log = LoggerFactory.getLogger(LimitadorDeTentativas.class);

    /** Uma regra: no máximo {@code maximo} tentativas na chave dentro da janela. */
    public record Limite(String chave, int maximo) {}

    /** Se pode seguir e, quando barrado, quanto falta para liberar (vira o Retry-After). */
    public record Resultado(boolean permitido, Duration tempoRestante) {}

    /**
     * Soma 1 em cada chave e garante o prazo, tudo numa ida só ao Redis: o Redis roda o script
     * inteiro sem nada no meio. Devolve {tentativas, ms restantes} de cada chave, em sequência.
     * (Com Redis em cluster, as chaves teriam que cair no mesmo slot; aqui é um Redis só.)
     */
    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> CONTAR = RedisScript.of("""
            local resposta = {}
            for _, chave in ipairs(KEYS) do
              local tentativas = redis.call('INCR', chave)
              local restante = redis.call('PTTL', chave)
              if restante < 0 then
                redis.call('PEXPIRE', chave, ARGV[1])
                restante = tonumber(ARGV[1])
              end
              resposta[#resposta + 1] = tentativas
              resposta[#resposta + 1] = restante
            end
            return resposta
            """, List.class);

    /** Desconta 1 só se a chave ainda existe e está acima de zero (o DECR mantém o prazo). */
    private static final RedisScript<Long> DEVOLVER = RedisScript.of("""
            local atual = tonumber(redis.call('GET', KEYS[1]) or '0')
            if atual > 0 then
              return redis.call('DECR', KEYS[1])
            end
            return 0
            """, Long.class);

    /** Teto do plano B: um atacante inventando chaves não consegue encher a memória. */
    static final int MAXIMO_DE_CHAVES_NO_PLANO_B = 10_000;

    /** Plano B: contagem desta cópia da aplicação, usada só enquanto o Redis estiver fora. */
    private record JanelaLocal(long tentativas, Instant fim) {}

    /**
     * Cache com tamanho máximo e prazo por chave (cada janela some quando acaba). Quando enche,
     * o próprio cache descarta as entradas menos usadas, sem varrer tudo a cada requisição.
     */
    private final Cache<String, JanelaLocal> planoB = Caffeine.newBuilder()
            .maximumSize(MAXIMO_DE_CHAVES_NO_PLANO_B)
            .expireAfter(Expiry.writing((String chave, JanelaLocal janela) -> tempoAte(janela.fim())))
            .build();

    /** Para avisar no log só quando o Redis cai e quando volta, e não a cada tentativa. */
    private final AtomicBoolean redisFora = new AtomicBoolean(false);

    private final StringRedisTemplate redis;

    public LimitadorDeTentativas(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public Resultado registrar(String chave, int limite, Duration janela) {
        return registrar(janela, new Limite(chave, limite));
    }

    /** Soma 1 em todos os limites e barra se qualquer um passou do máximo. */
    @SuppressWarnings("unchecked")
    public Resultado registrar(Duration janela, Limite... limites) {
        List<String> chaves = Arrays.stream(limites).map(Limite::chave).toList();
        try {
            List<Long> resposta = redis.execute(CONTAR, chaves, String.valueOf(janela.toMillis()));
            redisRespondeu();
            List<Contagem> contagens = new ArrayList<>();
            for (int i = 0; i < limites.length; i++) {
                contagens.add(new Contagem(limites[i], resposta.get(2 * i), Duration.ofMillis(resposta.get(2 * i + 1))));
            }
            return combinar(contagens);
        } catch (DataAccessException e) {
            redisFalhou(e);
            return registrarNaMemoria(janela, limites);
        }
    }

    /** Apaga a contagem das chaves (ex.: login certo). */
    public void zerar(String... chaves) {
        planoB.invalidateAll(List.of(chaves));
        try {
            redis.delete(List.of(chaves));
            redisRespondeu();
        } catch (DataAccessException e) {
            redisFalhou(e);
        }
    }

    /** Desconta uma tentativa da chave, sem apagar as outras (ex.: o login certo não conta contra o IP). */
    public void devolver(String chave) {
        planoB.asMap().computeIfPresent(chave, (k, janela) ->
                janela.tentativas() > 0 ? new JanelaLocal(janela.tentativas() - 1, janela.fim()) : janela);
        try {
            redis.execute(DEVOLVER, List.of(chave));
            redisRespondeu();
        } catch (DataAccessException e) {
            redisFalhou(e);
        }
    }

    private Resultado registrarNaMemoria(Duration janela, Limite... limites) {
        Instant agora = Instant.now();
        List<Contagem> contagens = new ArrayList<>();
        for (Limite limite : limites) {
            JanelaLocal janelaAtual = planoB.asMap().compute(limite.chave(), (k, anterior) ->
                    anterior == null || !anterior.fim().isAfter(agora)
                            ? new JanelaLocal(1, agora.plus(janela))                      // primeira ou janela vencida
                            : new JanelaLocal(anterior.tentativas() + 1, anterior.fim()));
            contagens.add(new Contagem(limite, janelaAtual.tentativas(), Duration.between(agora, janelaAtual.fim())));
        }
        return combinar(contagens);
    }

    private record Contagem(Limite limite, long tentativas, Duration restante) {
        boolean estourou() {
            return tentativas > limite.maximo();
        }
    }

    /** Barrado se qualquer limite estourou; a espera é a maior entre os que estouraram. */
    private static Resultado combinar(List<Contagem> contagens) {
        boolean algumEstourou = contagens.stream().anyMatch(Contagem::estourou);
        Duration espera = contagens.stream()
                .filter(c -> !algumEstourou || c.estourou())
                .map(Contagem::restante)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
        return new Resultado(!algumEstourou, espera);
    }

    private static Duration tempoAte(Instant fim) {
        Duration falta = Duration.between(Instant.now(), fim);
        return falta.isNegative() ? Duration.ZERO : falta;
    }

    private void redisRespondeu() {
        if (redisFora.compareAndSet(true, false)) {
            log.info("Redis voltou: o limite de tentativas volta a ser compartilhado entre as cópias da aplicação");
        }
    }

    private void redisFalhou(DataAccessException e) {
        if (redisFora.compareAndSet(false, true)) {
            log.warn("Redis indisponível: contando tentativas na memória (plano B) até ele voltar. Causa: {}", e.getMessage());
        }
    }
}
