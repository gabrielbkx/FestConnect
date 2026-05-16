package com.gabriel.party.dtos.midia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Metadados da mídia. O tipo (foto/vídeo) é deduzido pelo backend a partir do MIME type do arquivo.")
public record MidiaRequestDTO(

        @Schema(description = "Posição de exibição da mídia na galeria (menor número = exibido primeiro)", example = "1")
        Integer ordem,

        @Schema(description = "ID do item do catálogo ao qual a mídia será vinculada. Obrigatório.")
        @NotNull UUID itemCatalogoId
) {}
