package com.gabriel.party.dtos.itemcatalogo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Versão reduzida de um item do catálogo — usada nas listagens de busca")
public record ItemCatalogoResumoDTO(

        @Schema(description = "ID único do item")
        UUID id,

        @Schema(description = "Título do anúncio", example = "Buffet Completo para 100 Pessoas")
        String titulo,

        @Schema(description = "Preço base em R$", example = "3500.00")
        BigDecimal precoBase,

        @Schema(description = "Tipo do item: produto, servico ou local", example = "servico")
        String tipo,

        @Schema(description = "ID da categoria do item")
        UUID categoriaId,

        @Schema(description = "Nome da categoria do item", example = "Buffet")
        String categoriaNome,

        @Schema(description = "ID do prestador dono do item")
        UUID prestadorId,

        @Schema(description = "Nome do prestador", example = "Buffet Silva & Cia")
        String prestadorNome,

        @Schema(description = "Cidade do prestador", example = "São Paulo")
        String cidade,

        @Schema(description = "Estado do prestador (UF)", example = "SP")
        String estado,

        @Schema(description = "URL da primeira foto do item — null se nenhuma cadastrada")
        String fotoPrincipalUrl,

        @Schema(description = "Média das avaliações do item (1 a 5) — null se nenhuma avaliação")
        Double mediaAvaliacao,

        @Schema(description = "Total de avaliações do item", example = "25")
        Long totalAvaliacoes
) {}