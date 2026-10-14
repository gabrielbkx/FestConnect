package com.gabriel.party.config.infra.security;

import com.gabriel.party.config.infra.redis.DisjuntorDoRedis;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Tokens de login cancelados antes de vencer (logout de um dispositivo). Cada token cancelado vira
 * uma chave no Redis com prazo igual ao tempo que faltava para ele vencer: depois disso o token não
 * valeria de qualquer jeito, então o próprio Redis apaga a chave e a lista nunca cresce sem limite.
 * <p>
 * Se o Redis cair, a checagem DEIXA PASSAR (fail-open): barrar faria todo mundo perder o acesso
 * junto com o Redis. O que se perde é pouco: um token cancelado em outra cópia da aplicação volta a
 * valer até vencer (no máximo 2 h). Os que esta cópia já sabe que foram cancelados continuam barrados,
 * porque ficam também na memória. Para um incidente de verdade (aparelho roubado), o "sair de todos os
 * dispositivos" fica no banco e não depende do Redis.
 */
@Component
public class TokensRevogados {

    private static final String PREFIXO = "jwt:revogado:";

    /** Teto de cada memória local (cada entrada some sozinha quando o token venceria). */
    static final int MAXIMO_NA_MEMORIA = 100_000;

    /** Cancelados que esta cópia conhece: os que ela cancelou e os que o Redis confirmou. */
    private final Cache<String, Instant> revogadosAqui = novaMemoria();

    /** Cancelados com o Redis fora: vão para o Redis assim que ele responder, senão as outras cópias nunca saberiam. */
    private final Cache<String, Instant> faltaGravarNoRedis = novaMemoria();

    private final StringRedisTemplate redis;
    private final DisjuntorDoRedis disjuntor;

    public TokensRevogados(StringRedisTemplate redis, DisjuntorDoRedis disjuntor) {
        this.redis = redis;
        this.disjuntor = disjuntor;
    }

    /** Cancela o token até a hora em que ele venceria. Token já vencido não precisa ser guardado. */
    public void revogar(TokenDeLogin token) {
        if (token.id() == null || token.expiraEm() == null) {
            return;
        }
        Duration falta = tempoAte(token.expiraEm());
        if (falta.isZero()) {
            return;
        }
        revogadosAqui.put(token.id(), token.expiraEm());
        if (gravarNoRedis(token.id(), falta)) {
            reenviarOsQueFaltam();
        } else {
            faltaGravarNoRedis.put(token.id(), token.expiraEm());
        }
    }

    public boolean estaRevogado(TokenDeLogin token) {
        String id = token.id();
        if (id == null) {
            return false;
        }
        if (revogadosAqui.getIfPresent(id) != null) {
            return true;
        }
        if (!disjuntor.podeUsar()) {
            return false;
        }
        try {
            boolean revogado = Boolean.TRUE.equals(redis.hasKey(PREFIXO + id));
            disjuntor.deuCerto();
            if (revogado && token.expiraEm() != null) {
                // Cancelado em outra cópia: guardando aqui, as próximas checagens nem vão ao Redis
                // e o token continua barrado mesmo que o Redis caia
                revogadosAqui.put(id, token.expiraEm());
            }
            reenviarOsQueFaltam();
            return revogado;
        } catch (DataAccessException e) {
            disjuntor.falhou(e);
            return false;
        }
    }

    private boolean gravarNoRedis(String id, Duration falta) {
        if (!disjuntor.podeUsar()) {
            return false;
        }
        try {
            redis.opsForValue().set(PREFIXO + id, "1", falta);
            disjuntor.deuCerto();
            return true;
        } catch (DataAccessException e) {
            disjuntor.falhou(e);
            return false;
        }
    }

    /** O Redis acabou de responder: manda o que foi cancelado enquanto ele estava fora. */
    private void reenviarOsQueFaltam() {
        for (var pendente : faltaGravarNoRedis.asMap().entrySet()) {
            Duration falta = tempoAte(pendente.getValue());
            if (!falta.isZero() && !gravarNoRedis(pendente.getKey(), falta)) {
                return; // caiu de novo: tenta na próxima resposta do Redis
            }
            faltaGravarNoRedis.invalidate(pendente.getKey());
        }
    }

    private static Cache<String, Instant> novaMemoria() {
        return Caffeine.newBuilder()
                .maximumSize(MAXIMO_NA_MEMORIA)
                .expireAfter(Expiry.writing((String id, Instant expiraEm) -> tempoAte(expiraEm)))
                .build();
    }

    private static Duration tempoAte(Instant fim) {
        Duration falta = Duration.between(Instant.now(), fim);
        return falta.isNegative() ? Duration.ZERO : falta;
    }
}
