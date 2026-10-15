package com.gabriel.party.model.evento.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEvento {
    CASAMENTO("casamento", "Casamento"),
    QUINZE_ANOS("quinze_anos", "15 Anos"),
    ANIVERSARIO("aniversario", "Aniversário"),
    INFANTIL("infantil", "Festa Infantil"),
    CHURRASCO("churrasco", "Churrasco"),
    FORMATURA("formatura", "Formatura"),
    CORPORATIVO("corporativo", "Corporativo"),
    CONFRATERNIZACAO("confraternizacao", "Confraternização"),
    CHA_REVELACAO("cha_revelacao", "Chá Revelação"),
    BATIZADO("batizado", "Batizado"),
    OUTRO("outro", "Outro");

    private final String valor;
    /** Rótulo legível para mostrar na tela ("Aniversário" em vez de "ANIVERSARIO"). Fonte única. */
    private final String rotulo;

    TipoEvento(String valor, String rotulo) {
        this.valor = valor;
        this.rotulo = rotulo;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    public String getRotulo() {
        return rotulo;
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
