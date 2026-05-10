package com.gabriel.party.dtos.avaliacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Dados para criar uma nova avaliação de prestador")
public record AvaliacaoCreateDTO(

        @Schema(description = "Nota de 1 (péssimo) a 5 (excelente)", example = "4")
        @NotNull @Min(1) @Max(5)
        Integer nota,

        @Schema(description = "Comentário opcional sobre a experiência", example = "Ótimo profissional, muito pontual!")
        String comentario,

        @Schema(description = "ID do prestador a ser avaliado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull
        UUID prestadorId
) {}
