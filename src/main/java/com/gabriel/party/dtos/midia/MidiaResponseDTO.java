package com.gabriel.party.dtos.midia;

import com.gabriel.party.model.midia.enums.TipoMidia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados de uma mídia (foto ou vídeo)")
public record MidiaResponseDTO(

        @Schema(description = "ID único da mídia")
        UUID id,

        @Schema(description = "URL pública de acesso à mídia no S3", example = "https://s3.amazonaws.com/festconnect/...")
        String url,

        @Schema(description = "Tipo da mídia: IMAGEM ou VIDEO", example = "IMAGEM")
        TipoMidia tipo,

        @Schema(description = "Posição de exibição na galeria (menor número = primeiro)", example = "1")
        Integer ordem,

        @Schema(description = "ID do item do catálogo ao qual a mídia está vinculada.")
        UUID itemCatalogoId
) {}
