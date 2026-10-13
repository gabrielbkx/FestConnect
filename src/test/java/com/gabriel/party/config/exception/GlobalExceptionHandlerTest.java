package com.gabriel.party.config.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
}
