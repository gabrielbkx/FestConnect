package com.gabriel.party.dtos.avaliacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar uma avaliação existente")
public record AvaliacaoRequestDTO(

        @Schema(description = "Nova nota de 1 (péssimo) a 5 (excelente)", example = "5")
        @NotNull @Min(1) @Max(5)
        Integer nota,

        @Schema(description = "Novo comentário sobre a experiência", example = "Revisando minha avaliação — o serviço foi ainda melhor do que esperava.")
        String comentario
) {}
