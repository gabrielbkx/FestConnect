package com.gabriel.party.config.infra.security;

import java.time.Instant;

/**
 * Dados de um token de login já validado (assinatura, emissor e validade conferidos).
 *
 * @param email     dono do token (claim "sub")
 * @param emitidoEm quando o token foi emitido (claim "iat"); nulo em tokens anteriores a esta versão
 * @param id        identificador único do token (claim "jti"), usado no logout de um dispositivo (lista no Redis)
 * @param expiraEm  quando o token vence (claim "exp"); a revogação só precisa durar até aqui
 */
public record TokenDeLogin(String email, Instant emitidoEm, String id, Instant expiraEm) {
}
