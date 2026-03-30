package com.gabriel.party.dtos.midia;

import com.gabriel.party.model.midia.enums.TipoMidia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MidiaRequestDTO(
        @NotNull TipoMidia tipo,
        Integer ordem,
        @NotNull UUID prestadorId
) {}

