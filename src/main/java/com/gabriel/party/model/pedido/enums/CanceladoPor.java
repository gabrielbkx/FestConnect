package com.gabriel.party.model.pedido.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Quem cancelou um evento já reservado (#90). Fica guardado no pedido para, no futuro, virar métrica
 * de confiabilidade do parceiro e orientar a regra de arras (cliente desiste perde o sinal; parceiro
 * desiste devolve). No JSON vai em minúsculo; no banco é o name() via @Enumerated(STRING).
 */
public enum CanceladoPor {
    CLIENTE("cliente"),
    PRESTADOR("prestador");

    private final String valor;

    CanceladoPor(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static CanceladoPor fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        for (CanceladoPor c : values()) {
            if (c.valor.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        throw new IllegalArgumentException("Valor inválido para quem cancelou: '" + text + "'. Aceitos: cliente, prestador.");
    }
}
