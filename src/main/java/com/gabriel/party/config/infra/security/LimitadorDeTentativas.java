package com.gabriel.party.config.infra.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimitadorDeTentativas {

    private static final Logger log = LoggerFactory.getLogger(LimitadorDeTentativas.class);

    /** Plano B: contagem desta cópia da aplicação, usada só enquanto o Redis estiver fora. */
    private record JanelaLocal(long tentativas, Instant fim) {}
    private final Map<String, JanelaLocal> planoB = new ConcurrentHashMap<>();

    public record Resultado(boolean permitido, Duration tempoRestante) {}


    /** Soma 1 e garante o prazo num passo só: o Redis roda o script inteiro sem nada no meio. */
    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> CONTAR = RedisScript.of("""
            local tentativas = redis.call('INCR', KEYS[1])
            local restante = redis.call('PTTL', KEYS[1])
            if restante < 0 then
              redis.call('PEXPIRE', KEYS[1], ARGV[1])
              restante = tonumber(ARGV[1])
            end
            return {tentativas, restante}
            """, List.class);

    private final StringRedisTemplate redis;

    public LimitadorDeTentativas(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @SuppressWarnings("unchecked")
    public Resultado registrar(String chave, int limite, Duration janela) {
        try {
            List<Long> resposta = redis.execute(CONTAR, List.of(chave), String.valueOf(janela.toMillis()));
            long tentativas = resposta.get(0);
            Duration restante = Duration.ofMillis(resposta.get(1));
            return new Resultado(tentativas <= limite, restante);
        } catch (DataAccessException e) {
            log.warn("Redis indisponível, contando tentativas na memória (plano B): {}", e.getMessage());
            return registrarNaMemoria(chave, limite, janela);
        }
    }

    public void zerar(String chave) {
        planoB.remove(chave);
        try {
            redis.delete(chave);
        } catch (DataAccessException e) {
            log.warn("Redis indisponível ao zerar tentativas de {}: {}", chave, e.getMessage());
        }
    }


    private Resultado registrarNaMemoria(String chave, int limite, Duration janela) {
        Instant agora = Instant.now();
        JanelaLocal janelaAtual = planoB.compute(chave, (k, anterior) ->
                anterior == null || !anterior.fim().isAfter(agora)
                        ? new JanelaLocal(1, agora.plus(janela))                      // primeira ou janela vencida
                        : new JanelaLocal(anterior.tentativas() + 1, anterior.fim()));
        limparVencidas(agora);
        return new Resultado(janelaAtual.tentativas() <= limite, Duration.between(agora, janelaAtual.fim()));
    }

    /** Sem isso, alguém testando milhares de e-mails inventados enche a memória da aplicação. */
    private void limparVencidas(Instant agora) {
        if (planoB.size() > 10_000) {
            planoB.values().removeIf(j -> !j.fim().isAfter(agora));
        }
    }
}
