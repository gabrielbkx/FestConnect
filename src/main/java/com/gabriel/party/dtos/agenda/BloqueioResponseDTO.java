package com.gabriel.party.dtos.agenda;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Um bloqueio na agenda do prestador")
public record BloqueioResponseDTO(
        UUID id,
        LocalDateTime inicio,
        LocalDateTime fim,
        String motivo
) {}
