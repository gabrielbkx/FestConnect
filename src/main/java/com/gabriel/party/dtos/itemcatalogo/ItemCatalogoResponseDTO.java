package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Dados de um item do catálogo")
public record ItemCatalogoResponseDTO(

        @Schema(description = "ID único do item")
        UUID id,

        @Schema(description = "Título do anúncio", example = "Buffet Completo para 100 Pessoas")
        String titulo,

        @Schema(description = "Descrição detalhada")
        String descricao,

        @Schema(description = "Preço base em R$", example = "3500.00")
        BigDecimal precoBase,

        @Schema(description = "Tipo do item: PRODUTO, SERVICO ou LOCAL", example = "SERVICO")
        String tipo,

        @Schema(description = "Indica se o item está ativo no catálogo")
        boolean ativo,

        @Schema(description = "Mídias (fotos/vídeos) vinculadas a este item")
        List<MidiaResponseDTO> midias,

        @Schema(description = "Detalhes do espaço. Presente apenas quando tipo = LOCAL.")
        LocalDetalheDTO localDetalhe
) {}
