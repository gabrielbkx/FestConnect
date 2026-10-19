package com.gabriel.party.services.pagamento.gateway;

/**
 * Porta (hexagonal) para a intermediadora de pagamentos. O domínio fala só com esta interface; qual
 * provedor está por trás (Pagar.me, Mercado Pago, um fake local) é detalhe de infraestrutura (#99).
 * Trocar de provedor é só adicionar uma implementação, sem mexer no fluxo do pedido.
 */
public interface PagamentoGateway {

    /** Nome do provedor, guardado no pagamento para auditoria (ex.: "FAKE", "PAGARME"). */
    String nome();

    /** Cria a cobrança no provedor e devolve o identificador externo e o desfecho. */
    ResultadoCobranca cobrar(DadosCobranca dados);
}
