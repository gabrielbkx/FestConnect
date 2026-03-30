package com.gabriel.party.dtos.midia;

import com.gabriel.party.model.midia.enums.TipoMidia;

import java.util.UUID;

public record MidiaResponseDTO(
        UUID id,
        String url,
        TipoMidia tipo,
        Integer ordem,
        UUID prestadorId,
        String prestadorNome
) {}
