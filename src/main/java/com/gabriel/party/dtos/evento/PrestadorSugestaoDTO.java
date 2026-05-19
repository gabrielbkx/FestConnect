package com.gabriel.party.dtos.evento;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Prestador sugerido para um evento")
public record PrestadorSugestaoDTO(

        @Schema(description = "ID do prestador")
        UUID id,

        @Schema(description = "Nome do prestador")
        String nomeCompleto,

        @Schema(description = "URL da foto de perfil")
        String fotoPerfilUrl,

        @Schema(description = "Descrição curta do prestador")
        String descricao,

        @Schema(description = "Cidade do prestador")
        String cidade,

        @Schema(description = "Bairro do prestador")
        String bairro,

        @Schema(description = "Média de avaliações (0-5)")
        BigDecimal mediaAvaliacoes,

        @Schema(description = "Quantidade total de avaliações")
        Integer quantidadeAvaliacoes
) {}
