package com.gabriel.party.config.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SEGREDO = "segredo-so-de-teste";
    private static final String EMAIL = "cliente@festconnect.com";

    private TokenService tokenService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // O segredo vem de @Value em produção; no teste unitário não há Spring, então injetamos à mão
        ReflectionTestUtils.setField(tokenService, "segredo", SEGREDO);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL);
        usuario.setRole(Role.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("token de login válido devolve o e-mail do usuário")
    void tokenDeLogin_valido() {
        String token = tokenService.gerarToken(usuario);

        assertThat(tokenService.validarTokenDeLogin(token)).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("token de recuperação de senha NÃO serve como login")
    void tokenDeRecuperacao_naoServeComoLogin() {
        String tokenRecuperacao = tokenService.gerarTokenParaRecuperacaoDeSenha(usuario);

        assertThatThrownBy(() -> tokenService.validarTokenDeLogin(tokenRecuperacao))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ERRO_AO_VALIDAR_TOKEN_JWT);
    }

    @Test
    @DisplayName("token de recuperação continua valendo para redefinir a senha")
    void tokenDeRecuperacao_valeNaRedefinicao() {
        String tokenRecuperacao = tokenService.gerarTokenParaRecuperacaoDeSenha(usuario);

        assertThat(tokenService.validarTokenDeRecuperacaoDeSenha(tokenRecuperacao)).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("token de login NÃO serve para redefinir a senha")
    void tokenDeLogin_naoServeNaRedefinicao() {
        String token = tokenService.gerarToken(usuario);

        assertThatThrownBy(() -> tokenService.validarTokenDeRecuperacaoDeSenha(token))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("token vencido é recusado")
    void tokenVencido_recusado() {
        String vencido = JWT.create()
                .withIssuer("FestConnect")
                .withSubject(EMAIL)
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SEGREDO));

        assertThatThrownBy(() -> tokenService.validarTokenDeLogin(vencido))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ERRO_AO_VALIDAR_TOKEN_JWT);
    }

    @Test
    @DisplayName("token assinado com outro segredo é recusado")
    void tokenComOutroSegredo_recusado() {
        String forjado = JWT.create()
                .withIssuer("FestConnect")
                .withSubject(EMAIL)
                .withExpiresAt(Instant.now().plusSeconds(600))
                .sign(Algorithm.HMAC256("outro-segredo"));

        assertThatThrownBy(() -> tokenService.validarTokenDeLogin(forjado))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("texto que nem é JWT é recusado como AppException (e não estoura outra exceção)")
    void lixo_recusado() {
        assertThatThrownBy(() -> tokenService.validarTokenDeLogin("isso-nao-e-um-jwt"))
                .isInstanceOf(AppException.class);
    }
}
