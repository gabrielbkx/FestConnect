package com.gabriel.party.config.infra.security;

import java.time.Instant;

/**
 * Dados de um token de login já validado (assinatura, emissor e validade conferidos).
 *
 * @param email     dono do token (claim "sub")
 * @param emitidoEm quando o token foi emitido (claim "iat"); nulo em tokens anteriores a esta versão
 * @param id        identificador único do token (claim "jti"), usado na fase 2 do logout (denylist no Redis)
 */
public record TokenDeLogin(String email, Instant emitidoEm, String id) {
}
