package com.gabriel.party.config.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String segredo;

    public String gerarToken(Usuario usuario) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo);

            return JWT.create()
                    .withIssuer("FestConnect") // Emissor do token
                    .withSubject(usuario.getEmail()) // dono do token
                    .withClaim("role", usuario.getRole().getRole()) // Guardamos o perfil
                    .withExpiresAt((gerarDataExpiracao())) // Tempo de expiração do token
                    .sign(algorithm);

        } catch (Exception e) {
            throw new AppException(ErrorCode.ERRO_AO_GERAR_TOKEN_JWT, "Erro ao gerar token JWT");
        }

    }


    // toda vez que o metodo gerar token for chamado, o tempo de expiração será recalculado a partir daquele momento
    //Antes eu usava uma constante, mas com uma constante o tempo de expiração seria fixo, ou seja,
    // se o token fosse gerado as 10:00, ele expiraria as 12:00, mesmo que o token fosse gerado as 11:00,
    // ele expiraria as 12:00, ou seja, o tempo de expiração seria sempre as 12:00, independente do horário que o token fosse gerado
    public Instant gerarDataExpiracao() {
        return Instant.now().plusSeconds(7200); // 2 horas
    }

    public Instant gerarDataExpiracaoParaTokenDeREcuperacaoDeSenha() {
        return Instant.now().plusSeconds(300); // 5 minutos
    }


    public String validarTokenDeLogin(String token) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo);

            return JWT.require(algorithm)
                    .withIssuer("FestConnect")
                    .build()
                    .verify(token)
                    .getSubject();

        }catch (JWTVerificationException e){
            throw new AppException(ErrorCode.ERRO_AO_VALIDAR_TOKEN_JWT, "Token JWT inválido ou expirado");
        }
    }

    public String gerarTokenParaRecuperacaoDeSenha(Usuario usuario) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo);

            return JWT.create()
                    .withIssuer("FestConnect") // Emissor do token
                    .withSubject(usuario.getEmail()) // dono do token
                    .withClaim("tipo", "recuperacao_senha") // Guardamos o tipo do token para diferenciar
                    .withExpiresAt((gerarDataExpiracaoParaTokenDeREcuperacaoDeSenha())) // Tempo de expiração do token
                    .sign(algorithm);

        } catch (Exception e) {
            throw new AppException(ErrorCode.ERRO_AO_GERAR_TOKEN_JWT, "Erro ao gerar token JWT");
        }

    }
    public String validarTokenDeRecuperacaoDeSenha(String token) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(segredo);

            return JWT.require(algorithm)
                    .withIssuer("FestConnect")
                    .withClaim("tipo", "recuperacao_senha")
                    .build()
                    .verify(token)
                    .getSubject();

        }catch (JWTVerificationException e){
            throw new AppException(ErrorCode.ERRO_AO_VALIDAR_TOKEN_JWT, "Token JWT inválido ou expirado");
        }
    }
}