package com.gabriel.party.config.infra.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tokens de login cancelados antes de vencer (logout de um dispositivo). Cada token cancelado vira
 * uma chave no Redis com prazo igual ao tempo que faltava para ele vencer: depois disso o token não
 * valeria de qualquer jeito, então o próprio Redis apaga a chave e a lista nunca cresce sem limite.
 * <p>
 * Se o Redis cair, a checagem DEIXA PASSAR (fail-open): barrar faria todo mundo perder o acesso
 * junto com o Redis. O que se perde é pouco: um token cancelado em outra cópia da aplicação volta a
 * valer até vencer (no máximo 2 h). Os cancelados por esta cópia continuam barrados, porque ela também
 * os guarda na memória. Para um incidente de verdade (aparelho roubado), o "sair de todos os
 * dispositivos" fica no banco e não depende do Redis.
 */
@Component
public class TokensRevogados {

    private static final Logger log = LoggerFactory.getLogger(TokensRevogados.class);
    private static final String PREFIXO = "jwt:revogado:";

    /** Teto da memória local (cada entrada some sozinha quando o token venceria). */
    static final int MAXIMO_NA_MEMORIA = 100_000;

    private final Cache<String, Instant> revogadosAqui = Caffeine.newBuilder()
            .maximumSize(MAXIMO_NA_MEMORIA)
            .expireAfter(Expiry.writing((String id, Instant expiraEm) -> tempoAte(expiraEm)))
            .build();

    private final AtomicBoolean redisFora = new AtomicBoolean(false);

    private final StringRedisTemplate redis;

    public TokensRevogados(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** Cancela o token até a hora em que ele venceria. Token já vencido não precisa ser guardado. */
    public void revogar(String id, Instant expiraEm) {
        if (id == null || expiraEm == null) {
            return;
        }
        Duration falta = tempoAte(expiraEm);
        if (falta.isZero()) {
            return;
        }
        revogadosAqui.put(id, expiraEm);
        try {
            redis.opsForValue().set(PREFIXO + id, "1", falta);
            redisRespondeu();
        } catch (DataAccessException e) {
            redisFalhou(e);
        }
    }

    public boolean estaRevogado(String id) {
        if (id == null) {
            return false;
        }
        if (revogadosAqui.getIfPresent(id) != null) {
            return true;
        }
        try {
            boolean revogado = Boolean.TRUE.equals(redis.hasKey(PREFIXO + id));
            redisRespondeu();
            return revogado;
        } catch (DataAccessException e) {
            redisFalhou(e);
            return false;
        }
    }

    private static Duration tempoAte(Instant fim) {
        Duration falta = Duration.between(Instant.now(), fim);
        return falta.isNegative() ? Duration.ZERO : falta;
    }

    private void redisRespondeu() {
        if (redisFora.compareAndSet(true, false)) {
            log.info("Redis voltou: a lista de tokens cancelados volta a valer para todas as cópias da aplicação");
        }
    }

    private void redisFalhou(DataAccessException e) {
        if (redisFora.compareAndSet(false, true)) {
            log.warn("Redis indisponível: só os tokens cancelados por esta cópia da aplicação ficam barrados até ele voltar. Causa: {}",
                    e.getMessage());
        }
    }
}
