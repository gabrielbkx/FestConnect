package com.gabriel.party.controllers.prestador;

import com.gabriel.party.services.pedido.PedidoService;
import com.gabriel.party.services.prestador.PrestadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PrestadorControllerAgendaTest {

    @Mock private PrestadorService prestadorService;
    @Mock private PedidoService pedidoService;

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        mvc = MockMvcBuilders.standaloneSetup(new PrestadorController(prestadorService, pedidoService)).build();
    }

    @Test
    @DisplayName("as datas de/ate viram a janela do primeiro instante de 'de' até o fim do dia 'ate'")
    void agenda_converteDatasEmJanelaDeDiasInteiros() throws Exception {
        UUID prestadorId = UUID.randomUUID();
        when(pedidoService.agendaOcupada(eq(prestadorId), eq(LocalDateTime.of(2026, 12, 1, 0, 0)),
                eq(LocalDateTime.of(2027, 1, 1, 0, 0)))).thenReturn(List.of());

        mvc.perform(get("/api/v1/prestadores/{id}/agenda", prestadorId)
                        .param("de", "2026-12-01").param("ate", "2026-12-31"))
                .andExpect(status().isOk());

        // ate = 31/12 vira exclusivo em 01/01 (o dia 31 inteiro entra na janela)
        verify(pedidoService).agendaOcupada(prestadorId,
                LocalDateTime.of(2026, 12, 1, 0, 0), LocalDateTime.of(2027, 1, 1, 0, 0));
    }
}
