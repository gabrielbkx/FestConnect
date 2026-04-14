package com.gabriel.party.model.pedido.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.gabriel.party.model.midia.enums.TipoMidia;

public enum StatusPedido {
    PENDENTE("pendente"),
    ORCADO("orçado"),
    RECUSADO("recusado"),
    CANCELADO("cancelado"),
    ACEITO("aceito");

    private String valor;

    StatusPedido(String valor) {
        this.valor = valor;
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
                "orçado, recusado, cancelado.");
    }
}
