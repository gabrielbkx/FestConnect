package com.gabriel.party.dtos.avaliacao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados de uma avaliação")
public record AvaliacaoResponseDTO(

        @Schema(description = "ID da avaliação")
        UUID id,

        @Schema(description = "Nota atribuída (1 a 5)", example = "4")
        Integer nota,

        @Schema(description = "Comentário do cliente", example = "Muito profissional e pontual!")
        String comentario,

        @Schema(description = "Data e hora de criação da avaliação")
        LocalDateTime dataCriacao,

        @Schema(description = "ID do prestador avaliado")
        UUID prestadorId,

        @Schema(description = "Nome do prestador avaliado", example = "Buffet Silva & Cia")
        String prestadorNome,

        @Schema(description = "ID do cliente que fez a avaliação")
        UUID clienteId,

        @Schema(description = "Nome do cliente que fez a avaliação", example = "João da Silva")
        String clienteNome,

        @Schema(description = "ID do pedido que originou a avaliação")
        UUID pedidoId,

        @Schema(description = "ID do item do catálogo avaliado")
        UUID itemCatalogoId,

        @Schema(description = "Título do item do catálogo avaliado", example = "Buffet Completo para 100 Pessoas")
        String itemCatalogoTitulo
) {}
