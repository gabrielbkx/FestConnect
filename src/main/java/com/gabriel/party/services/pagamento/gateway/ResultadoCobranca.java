package com.gabriel.party.services.pagamento.gateway;

import com.gabriel.party.model.pagamento.enums.StatusPagamento;

/** O que o gateway devolve ao criar uma cobrança: o id no provedor, o desfecho e um link opcional. */
public record ResultadoCobranca(
        String idExterno,
        StatusPagamento status,
        String urlComprovante
) {}
