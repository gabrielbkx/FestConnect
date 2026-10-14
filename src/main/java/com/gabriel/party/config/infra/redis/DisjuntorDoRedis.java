package com.gabriel.party.config.infra.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Disjuntor (circuit breaker) das chamadas ao Redis, compartilhado por quem usa o Redis.
 * <p>
 * Problema que ele resolve: o REJECT_COMMANDS do {@link RedisConfig} só ajuda quando o Lettuce já sabe
 * que a conexão caiu. Se o Redis está "pendurado" (conexão aberta, mas sem responder: failover, rede
 * partida, Redis preso num comando lento), cada chamada espera o timeout inteiro (2 s). Como a lista
 * de tokens cancelados é consultada em TODA requisição autenticada, a API inteira ficaria 2 s mais lenta.
 * <p>
 * Como funciona: depois de uma falha, ninguém fala com o Redis durante {@link #PAUSA_DEPOIS_DE_FALHA}
 * (cada um usa o seu plano B na hora). Passada a pausa, UMA chamada testa o Redis: se responder, tudo
 * volta ao normal; se falhar, começa outra pausa.
 */
@Component
public class DisjuntorDoRedis {

    private static final Logger log = LoggerFactory.getLogger(DisjuntorDoRedis.class);

    static final Duration PAUSA_DEPOIS_DE_FALHA = Duration.ofSeconds(15);

    /** 0 = Redis respondendo. Senão, o instante (em ms) em que uma chamada pode testar o Redis de novo. */
    private static final long NORMAL = 0;

    private final AtomicLong proximaTentativaEm = new AtomicLong(NORMAL);

    private final LongSupplier relogio;

    public DisjuntorDoRedis() {
        this(System::currentTimeMillis);
    }

    /** Para os testes controlarem o tempo. */
    DisjuntorDoRedis(LongSupplier relogio) {
        this.relogio = relogio;
    }

    /** Pode chamar o Redis agora? Se não, use o plano B sem esperar. */
    public boolean podeUsar() {
        long proxima = proximaTentativaEm.get();
        if (proxima == NORMAL) {
            return true;
        }
        long agora = relogio.getAsLong();
        // Passada a pausa, só quem ganhar o compareAndSet testa o Redis; os outros seguem no plano B
        return agora >= proxima && proximaTentativaEm.compareAndSet(proxima, agora + PAUSA_DEPOIS_DE_FALHA.toMillis());
    }

    public void deuCerto() {
        if (proximaTentativaEm.getAndSet(NORMAL) != NORMAL) {
            log.info("Redis voltou: limite de tentativas e tokens cancelados voltam a valer para todas as cópias da aplicação");
        }
    }

    public void falhou(DataAccessException e) {
        long anterior = proximaTentativaEm.getAndSet(relogio.getAsLong() + PAUSA_DEPOIS_DE_FALHA.toMillis());
        if (anterior == NORMAL) {
            log.warn("Redis indisponível: limite de tentativas e tokens cancelados seguem na memória desta cópia, "
                    + "com nova tentativa a cada {} s. Causa: {}", PAUSA_DEPOIS_DE_FALHA.toSeconds(), e.getMessage());
        }
    }
}
