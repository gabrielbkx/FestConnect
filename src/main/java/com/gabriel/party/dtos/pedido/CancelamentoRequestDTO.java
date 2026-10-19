package com.gabriel.party.dtos.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para cancelar um evento já reservado")
public record CancelamentoRequestDTO(

        @Schema(description = "Motivo do cancelamento (obrigatório)", example = "Imprevisto com o local do evento")
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        String motivo
) {}
