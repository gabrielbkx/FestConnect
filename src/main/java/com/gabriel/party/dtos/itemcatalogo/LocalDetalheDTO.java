package com.gabriel.party.dtos.itemcatalogo;

import java.math.BigDecimal;

public record LocalDetalheDTO(
        Integer capacidadeMaxima,
        BigDecimal metragem,
        Boolean permiteSom,
        Boolean temEstacionamento,
        String tipoEspaco
) {}
