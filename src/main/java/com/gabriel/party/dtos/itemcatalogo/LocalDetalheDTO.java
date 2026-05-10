package com.gabriel.party.dtos.itemcatalogo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Detalhes de um item do tipo LOCAL (espaço para eventos). Obrigatório apenas quando tipo = LOCAL.")
public record LocalDetalheDTO(

        @Schema(description = "Capacidade máxima de pessoas", example = "200")
        Integer capacidadeMaxima,

        @Schema(description = "Área do espaço em m²", example = "350.00")
        BigDecimal metragem,

        @Schema(description = "Se o espaço permite uso de equipamentos de som", example = "true")
        Boolean permiteSom,

        @Schema(description = "Se o espaço possui estacionamento próprio", example = "false")
        Boolean temEstacionamento,

        @Schema(description = "Tipo ou característica do espaço", example = "Salão de festas coberto")
        String tipoEspaco
) {}
