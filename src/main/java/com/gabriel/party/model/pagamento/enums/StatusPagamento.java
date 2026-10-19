package com.gabriel.party.model.pagamento.enums;

/**
 * Situação de um pagamento perante o gateway. PENDENTE cobre provedores assíncronos (o cliente foi
 * para um checkout e a confirmação chega depois, por webhook); APROVADO/RECUSADO são o desfecho.
 */
public enum StatusPagamento {
    PENDENTE,
    APROVADO,
    RECUSADO
}
