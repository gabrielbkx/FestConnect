package com.gabriel.party.dtos.midia;

import com.gabriel.party.model.midia.enums.TipoMidia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Dados para associar metadados à mídia enviada via upload")
public record MidiaRequestDTO(

        @Schema(description = "Tipo da mídia enviada", allowableValues = {"IMAGEM", "VIDEO"})
        @NotNull TipoMidia tipo,

        @Schema(description = "Posição de exibição da mídia na galeria (menor número = exibido primeiro)", example = "1")
        Integer ordem,

        @Schema(description = "ID do item do catálogo ao qual a mídia será vinculada. Opcional — se omitido, a mídia fica vinculada apenas ao perfil do prestador.")
        UUID itemCatalogoId
) {}
