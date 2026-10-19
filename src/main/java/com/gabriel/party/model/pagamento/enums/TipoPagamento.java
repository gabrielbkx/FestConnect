package com.gabriel.party.model.pagamento.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * O que está sendo pago. Hoje só o SINAL (arras) que reserva a data; o saldo/plano entra depois.
 * No JSON vai em minúsculo; no banco é o name() via @Enumerated(STRING).
 */
public enum TipoPagamento {
    SINAL("sinal");

    private final String valor;

    TipoPagamento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static TipoPagamento fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        for (TipoPagamento tipo : values()) {
            if (tipo.valor.equalsIgnoreCase(text.trim())) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de pagamento inválido: '" + text + "'. Valores aceitos: sinal.");
    }
}
