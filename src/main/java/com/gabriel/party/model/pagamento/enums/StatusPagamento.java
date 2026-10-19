package com.gabriel.party.model.pagamento.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Situação de um pagamento perante o gateway. PENDENTE cobre provedores assíncronos (o cliente foi
 * para um checkout e a confirmação chega depois, por webhook); APROVADO/RECUSADO são o desfecho.
 * No JSON vai em minúsculo (igual a StatusPedido); no banco é o name() via @Enumerated(STRING).
 */
public enum StatusPagamento {
    PENDENTE("pendente"),
    APROVADO("aprovado"),
    RECUSADO("recusado");

    private final String valor;

    StatusPagamento(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusPagamento fromValue(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        for (StatusPagamento status : values()) {
            if (status.valor.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de pagamento inválido: '" + text + "'. Valores aceitos: pendente, aprovado, recusado.");
    }
}
