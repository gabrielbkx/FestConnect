package com.gabriel.party.dtos.autenticacao.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoPerfil {
    PRESTADOR("prestador"),
    CLIENTE("cliente")
    ;

    private final String valor;

    TipoPerfil(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoPerfil fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        for (TipoPerfil tipo : TipoPerfil.values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de perfil inválido: '" + text + "'. Valores aceitos: prestador, cliente.");
    }
}
