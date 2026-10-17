package com.gabriel.party.dtos.agenda;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Dados para o prestador bloquear um período na própria agenda")
public record BloqueioRequestDTO(

        @Schema(description = "Início do período bloqueado", example = "2026-12-20T18:00:00")
        @NotNull(message = "O início do bloqueio é obrigatório")
        LocalDateTime inicio,

        @Schema(description = "Fim do período bloqueado", example = "2026-12-20T23:00:00")
        @NotNull(message = "O fim do bloqueio é obrigatório")
        LocalDateTime fim,

        @Schema(description = "Motivo opcional", example = "Evento particular fora da plataforma")
        String motivo
) {}
