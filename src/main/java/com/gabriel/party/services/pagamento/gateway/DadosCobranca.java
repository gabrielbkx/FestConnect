package com.gabriel.party.services.pagamento.gateway;

import java.math.BigDecimal;
import java.util.UUID;

/** Dados que o domínio passa ao gateway para abrir uma cobrança. */
public record DadosCobranca(
        BigDecimal valor,
        String descricao,
        UUID referenciaPedido
) {}
