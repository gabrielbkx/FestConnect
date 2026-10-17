package com.gabriel.party.model.pedido.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum StatusPedido {
    PENDENTE("pendente"),
    ORCADO("orçado"),
    RECUSADO("recusado"),
    CANCELADO("cancelado"),
    RESERVADO("reservado"),
    EXPIRADO("expirado");

    private String valor;

    StatusPedido(String valor) {
        this.valor = valor;
    }

    /**
     * Para onde cada status pode ir. Quem não está no mapa (RECUSADO, CANCELADO, RESERVADO, EXPIRADO)
     * é terminal: não vai para lugar nenhum. A #90 abre as transições a partir de RESERVADO.
     * Isto é só a máquina de estados; QUEM pode disparar cada transição é checado nas permissões.
     */
    private static final Map<StatusPedido, Set<StatusPedido>> TRANSICOES = Map.of(
            PENDENTE, EnumSet.of(ORCADO, RECUSADO, CANCELADO, EXPIRADO),
            ORCADO, EnumSet.of(RESERVADO, RECUSADO, CANCELADO, EXPIRADO)
    );

    /** O pedido neste status pode passar para {@code novo}? */
    public boolean podeIrPara(StatusPedido novo) {
        return novo != null && TRANSICOES.getOrDefault(this, EnumSet.noneOf(StatusPedido.class)).contains(novo);
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusPedido fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (StatusPedido tipo : StatusPedido.values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de pedido inválido: '" + text + "'. Valores aceitos: pendente, " +
                "orçado, recusado, cancelado, reservado, expirado.");
    }
}
