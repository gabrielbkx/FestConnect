package com.gabriel.party.model.evento.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEvento {
    PLANEJANDO("planejando"),
    CONFIRMADO("confirmado"),
    CANCELADO("cancelado");

    private final String valor;

    StatusEvento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusEvento fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (StatusEvento status : StatusEvento.values()) {
            if (status.valor.equalsIgnoreCase(text.trim()) || status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status de evento inválido: '" + text + "'.");
    }
}