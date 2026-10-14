package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.services.autenticacao.SessaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("logout cancela só o token desta requisição (sem o prefixo Bearer) e responde 204")
    void logout_cancelaOTokenDestaRequisicao() throws Exception {
        mvc.perform(post("/api/v1/sessao/logout").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isNoContent());

        verify(sessaoService).encerrarEsteDispositivo("abc.def.ghi");
    }
}
