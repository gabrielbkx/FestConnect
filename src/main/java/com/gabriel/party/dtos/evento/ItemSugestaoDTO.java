package com.gabriel.party.dtos.evento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Serviço do prestador disponível nesta categoria")
public record ItemSugestaoDTO(
        @Schema(description = "ID do item do catálogo") UUID id,
        @Schema(description = "Título do serviço") String titulo,
        @Schema(description = "Preço base") BigDecimal precoBase
) {}
