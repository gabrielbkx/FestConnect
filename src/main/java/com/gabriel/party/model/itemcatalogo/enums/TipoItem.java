package com.gabriel.party.model.itemcatalogo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoItem {
    PRODUTO("produto"),
    SERVICO("servico")
    ;

    private final String valor;

    TipoItem(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoItem fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (TipoItem tipo : TipoItem.values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de item inválido: '" + text + "'. Valores aceitos: produto, servico.");
    }
}
