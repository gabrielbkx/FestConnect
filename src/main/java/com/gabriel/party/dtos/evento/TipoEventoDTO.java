package com.gabriel.party.dtos.evento;

import com.gabriel.party.model.evento.enums.TipoEvento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Tipo de evento disponível e suas categorias de serviço sugeridas")
public record TipoEventoDTO(

        @Schema(description = "Identificador do tipo")
        TipoEvento tipo,

        @Schema(description = "Rótulo amigável para exibição", example = "Casamento")
        String label,

        @Schema(description = "Categorias de serviço sugeridas para este tipo de evento")
        List<String> categoriasSugeridas
) {}
