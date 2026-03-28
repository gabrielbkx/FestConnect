package com.gabriel.party.model.midia.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoMidia {
    VIDEO("video"),
    FOTO("foto"),
    ;

    private final String valor;


    TipoMidia(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoMidia fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (TipoMidia tipo : TipoMidia.values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de mídia inválido: '" + text + "'. Valores aceitos: video, foto.");
    }
}
