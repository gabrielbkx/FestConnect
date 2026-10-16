package com.gabriel.party.config.exception;

import com.gabriel.party.exceptions.MuitasTentativasException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("violação da restrição de exclusão (SQLState 23P01) vira 409 de conflito de agenda")
    void violacaoDeExclusao_responde409() {
        var excecao = new DataIntegrityViolationException("conflito", new SQLException("exclusion", "23P01"));

        var resposta = handler.handleViolacaoDeIntegridade(excecao, new MockHttpServletRequest("PUT", "/api/v1/pedidos/x/aceitar"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().code()).isEqualTo("pedido_conflito_agenda");
    }

    @Test
    @DisplayName("outra violação de integridade (não 23P01) continua sendo 500 inesperado")
    void outraViolacaoDeIntegridade_responde500() {
        var excecao = new DataIntegrityViolationException("fk", new SQLException("fk violation", "23503"));

        var resposta = handler.handleViolacaoDeIntegridade(excecao, new MockHttpServletRequest("POST", "/api/v1/pedidos"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resposta.getBody().code()).isEqualTo("erro_inesperado");
    }

    @Test
    @DisplayName("negação do @PreAuthorize cai no handler de acesso negado, e não no genérico (500)")
    void authorizationDenied_resolveParaHandlerDeAcessoNegado() {
        var excecao = new AuthorizationDeniedException("Access Denied");

        // O Spring escolhe o @ExceptionHandler de tipo mais próximo da exceção lançada
        var metodo = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class).resolveMethodByThrowable(excecao);

        assertThat(metodo).isNotNull();
        assertThat(metodo.getName()).isEqualTo("handleAccessDenied");
    }

    @Test
    @DisplayName("acesso negado responde 403 com o código acesso_negado")
    void handleAccessDenied_responde403() {
        var request = new MockHttpServletRequest("GET", "/api/v1/clientes");

        var resposta = handler.handleAccessDenied(new AuthorizationDeniedException("Access Denied"), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(resposta.getBody()).isNotNull();
    }

    @Test
    @DisplayName("JSON malformado resolve para o handler de corpo ilegível, e não para o genérico (500)")
    void corpoIlegivel_resolveParaHandlerProprio() {
        var excecao = new HttpMessageNotReadableException("JSON parse error", (org.springframework.http.HttpInputMessage) null);

        var metodo = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class).resolveMethodByThrowable(excecao);

        assertThat(metodo).isNotNull();
        assertThat(metodo.getName()).isEqualTo("handleCorpoIlegivel");
    }

    @Test
    @DisplayName("corpo ilegível responde 400 com o código corpo_invalido")
    void handleCorpoIlegivel_responde400() {
        var request = new MockHttpServletRequest("POST", "/api/v1/pedidos");

        var resposta = handler.handleCorpoIlegivel(
                new HttpMessageNotReadableException("x", (org.springframework.http.HttpInputMessage) null), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().code()).isEqualTo("corpo_invalido");
    }

    @Test
    @DisplayName("rota inexistente resolve para o handler de 404, e não para o genérico (500)")
    void rotaInexistente_resolveParaHandlerProprio() {
        var excecao = new NoResourceFoundException(HttpMethod.GET, "/api/v1/rota-que-nao-existe", "/api/v1/rota-que-nao-existe");

        var metodo = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class).resolveMethodByThrowable(excecao);

        assertThat(metodo).isNotNull();
        assertThat(metodo.getName()).isEqualTo("handleRotaInexistente");
    }

    @Test
    @DisplayName("rota inexistente responde 404 com o código recurso_nao_encontrado")
    void handleRotaInexistente_responde404() {
        var request = new MockHttpServletRequest("GET", "/api/v1/rota-que-nao-existe");

        var resposta = handler.handleRotaInexistente(
                new NoResourceFoundException(HttpMethod.GET, "/api/v1/rota-que-nao-existe", "/api/v1/rota-que-nao-existe"), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().code()).isEqualTo("recurso_nao_encontrado");
    }

    @Test
    @DisplayName("muitas tentativas cai no handler próprio, e não no da AppException")
    void muitasTentativas_resolveParaHandlerProprio() {
        var excecao = new MuitasTentativasException(Duration.ofSeconds(30));

        var metodo = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class).resolveMethodByThrowable(excecao);

        assertThat(metodo).isNotNull();
        assertThat(metodo.getName()).isEqualTo("handleMuitasTentativas");
    }

    @Test
    @DisplayName("muitas tentativas responde 429 com Retry-After em segundos, arredondado para cima")
    void muitasTentativas_responde429ComRetryAfter() {
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        var resposta = handler.handleMuitasTentativas(new MuitasTentativasException(Duration.ofMillis(839_200)), request);

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(resposta.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("840");
        assertThat(resposta.getBody().message()).isEqualTo("Muitas tentativas. Tente de novo em 14 minuto(s).");
    }

    @Test
    @DisplayName("mensagem e Retry-After nunca discordam: 60,4 s viram 61 s e 2 minutos")
    void muitasTentativas_mensagemERetryAfterConcordam() {
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/login");

        var resposta = handler.handleMuitasTentativas(new MuitasTentativasException(Duration.ofMillis(60_400)), request);

        assertThat(resposta.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo("61");
        assertThat(resposta.getBody().message()).isEqualTo("Muitas tentativas. Tente de novo em 2 minuto(s).");
    }
}
