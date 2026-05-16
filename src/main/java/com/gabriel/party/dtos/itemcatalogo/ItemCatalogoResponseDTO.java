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

        @Schema(description = "ID da categoria do item")
        UUID categoriaId,

        @Schema(description = "Nome da categoria do item", example = "Buffet")
        String categoriaNome,

        @Schema(description = "Indica se o item está ativo no catálogo")
        boolean ativo,

        @Schema(description = "Mídias (fotos/vídeos) vinculadas a este item")
        List<MidiaResponseDTO> midias,

        @Schema(description = "Detalhes do espaço. Presente apenas quando tipo = LOCAL.")
        LocalDetalheDTO localDetalhe,

        @Schema(description = "ID do prestador dono do item")
        UUID prestadorId,

        @Schema(description = "Nome do prestador", example = "Buffet Silva & Cia")
        String prestadorNome,

        @Schema(description = "URL da foto de perfil do prestador")
        String prestadorFotoUrl,

        @Schema(description = "Cidade do prestador", example = "São Paulo")
        String prestadorCidade,

        @Schema(description = "Estado do prestador (UF)", example = "SP")
        String prestadorEstado
) {}
