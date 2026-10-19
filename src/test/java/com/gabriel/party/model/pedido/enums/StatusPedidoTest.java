package com.gabriel.party.model.pedido.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.gabriel.party.model.pedido.enums.StatusPedido.AGUARDANDO_ENTRADA;
import static com.gabriel.party.model.pedido.enums.StatusPedido.RESERVADO;
import static com.gabriel.party.model.pedido.enums.StatusPedido.CANCELADO;
import static com.gabriel.party.model.pedido.enums.StatusPedido.EXPIRADO;
import static com.gabriel.party.model.pedido.enums.StatusPedido.ORCADO;
import static com.gabriel.party.model.pedido.enums.StatusPedido.PENDENTE;
import static com.gabriel.party.model.pedido.enums.StatusPedido.RECUSADO;
import static org.assertj.core.api.Assertions.assertThat;

class StatusPedidoTest {

    // A máquina de estados esperada. Tudo que não está aqui tem que ser proibido.
    private static final Map<StatusPedido, Set<StatusPedido>> PERMITIDAS = Map.of(
            PENDENTE, EnumSet.of(ORCADO, RECUSADO, CANCELADO, EXPIRADO),
            ORCADO, EnumSet.of(AGUARDANDO_ENTRADA, RECUSADO, CANCELADO, EXPIRADO),
            AGUARDANDO_ENTRADA, EnumSet.of(RESERVADO, CANCELADO, EXPIRADO)
    );

    @Test
    @DisplayName("para cada par de status, só as transições do mapa são permitidas")
    void podeIrPara_cobreMatrizInteira() {
        for (StatusPedido de : StatusPedido.values()) {
            Set<StatusPedido> permitidas = PERMITIDAS.getOrDefault(de, EnumSet.noneOf(StatusPedido.class));
            for (StatusPedido para : StatusPedido.values()) {
                assertThat(de.podeIrPara(para))
                        .as("%s → %s", de, para)
                        .isEqualTo(permitidas.contains(para));
            }
        }
    }

    @Test
    @DisplayName("status terminais não vão para lugar nenhum")
    void statusTerminais_naoTransicionam() {
        for (StatusPedido terminal : EnumSet.of(RESERVADO, RECUSADO, CANCELADO, EXPIRADO)) {
            for (StatusPedido para : StatusPedido.values()) {
                assertThat(terminal.podeIrPara(para))
                        .as("terminal %s → %s", terminal, para)
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("ninguém transiciona para um status nulo")
    void novoNulo_naoTransiciona() {
        for (StatusPedido de : StatusPedido.values()) {
            assertThat(de.podeIrPara(null)).as("%s → null", de).isFalse();
        }
    }
}
