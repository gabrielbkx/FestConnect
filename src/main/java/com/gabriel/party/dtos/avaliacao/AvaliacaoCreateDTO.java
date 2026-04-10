package com.gabriel.party.dtos.avaliacao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AvaliacaoCreateDTO(
        @NotNull
        @Min(1) @Max(5)
        Integer nota,
        String comentario,
        @NotNull
        UUID prestadorId) {


}
