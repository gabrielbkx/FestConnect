package com.gabriel.party.model.evento.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEvento {
    CASAMENTO("casamento"),
    QUINZE_ANOS("quinze_anos"),
    ANIVERSARIO("aniversario"),
    INFANTIL("infantil"),
    CHURRASCO("churrasco"),
    FORMATURA("formatura"),
    CORPORATIVO("corporativo"),
    CONFRATERNIZACAO("confraternizacao"),
    CHA_REVELACAO("cha_revelacao"),
    BATIZADO("batizado"),
    OUTRO("outro");

    private final String valor;

    TipoEvento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoEvento fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (TipoEvento tipo : TipoEvento.values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim()) || tipo.name().equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de evento inválido: '" + text + "'.");
    }
}
