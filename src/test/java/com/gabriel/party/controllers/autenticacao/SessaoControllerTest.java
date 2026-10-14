package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.config.infra.security.TokenDeLogin;
import com.gabriel.party.services.autenticacao.SessaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.CurrentSecurityContextArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SessaoControllerTest {

    @Mock private SessaoService sessaoService;

    @InjectMocks private SessaoController controller;

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        // No app, o Spring Security registra este resolvedor sozinho; aqui (sem contexto) é na mão
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new CurrentSecurityContextArgumentResolver())
                .build();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("logout cancela o token que o SecurityFilter validou (guardado na autenticação) e responde 204")
    void logout_cancelaOTokenDestaRequisicao() throws Exception {
        var tokenDeLogin = new TokenDeLogin("ana@x.com", Instant.now(), "jti-deste-aparelho", Instant.now().plusSeconds(3600));
        var autenticacao = new UsernamePasswordAuthenticationToken("ana", null, List.of());
        autenticacao.setDetails(tokenDeLogin);
        SecurityContextHolder.getContext().setAuthentication(autenticacao);

        mvc.perform(post("/api/v1/sessao/logout"))
                .andExpect(status().isNoContent());

        verify(sessaoService).encerrarEsteDispositivo(tokenDeLogin);
    }
}
