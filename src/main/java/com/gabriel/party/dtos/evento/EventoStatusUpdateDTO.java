package com.gabriel.party.dtos.evento;

import com.gabriel.party.model.evento.enums.StatusEvento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Atualiza apenas o status do evento")
public record EventoStatusUpdateDTO(

        @Schema(description = "Novo status do evento", example = "confirmado",
                allowableValues = {"planejando", "confirmado", "cancelado"})
        @NotNull(message = "O status é obrigatório")
        StatusEvento status
) {}
