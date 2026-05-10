package com.gabriel.party.dtos.prestador;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resumo de um prestador para exibição em listagens e cards. " +
        "Use GET /prestadores/{id} para o perfil completo com catálogo, mídias e avaliações.")
public record PrestadorSummaryDTO(

        @Schema(description = "ID único do prestador")
        UUID id,

        @Schema(description = "Nome completo ou nome fantasia", example = "Buffet Silva & Cia")
        String nome,

        @Schema(description = "URL da foto de perfil")
        String fotoPerfilUrl,

        @Schema(description = "Descrição resumida dos serviços", example = "Buffet especializado em casamentos e eventos corporativos.")
        String descricao,

        @Schema(description = "Nome da categoria de serviço", example = "Buffet")
        String categoriaNome,

        @Schema(description = "Cidade onde o prestador atua", example = "São Paulo")
        String cidade,

        @Schema(description = "Estado onde o prestador atua", example = "SP")
        String estado,

        @Schema(description = "Média das notas das avaliações ativas (1.0 a 5.0). Nulo se não houver avaliações.", example = "4.3")
        Double mediaAvaliacoes,

        @Schema(description = "Total de avaliações ativas recebidas", example = "27")
        Integer quantidadeAvaliacoes
) {}
