package com.gabriel.party.dtos.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados para solicitar um orçamento a um prestador")
public record PedidoRequestDTO(

        @Schema(description = "ID do prestador que receberá o pedido", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "O ID do prestador é obrigatório")
        UUID prestadorId,

        @Schema(description = "ID do item do catálogo solicitado pelo cliente", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "O item do catálogo é obrigatório")
        UUID itemCatalogoId,

        @Schema(description = "Data e hora do evento. Deve ser uma data futura. Formato: ISO-8601", example = "2026-12-31T18:00:00")
        @NotNull(message = "A data do evento é obrigatória")
        @Future(message = "A data do evento deve ser no futuro")
        LocalDateTime dataEvento,

        @Schema(description = "Nome ou endereço do local do evento", example = "Salão de Festas do Condomínio Primavera, Rua das Flores, 100")
        @NotBlank(message = "O local do evento é obrigatório")
        String localEvento,

        @Schema(description = "Tipo ou tema do evento", example = "Casamento")
        @NotBlank(message = "O tipo de evento é obrigatório")
        String tipoEvento,

        @Schema(description = "Número estimado de convidados", example = "150")
        @NotNull(message = "O número de convidados é obrigatório")
        @Min(value = 1, message = "O evento deve ter pelo menos 1 convidado")
        Integer numeroConvidados,

        @Schema(description = "Descrição detalhada do que o cliente precisa do prestador", example = "Preciso de um buffet completo com coquetel de boas-vindas, jantar e mesa de doces.")
        @NotBlank(message = "A descrição do pedido é obrigatória")
        String descricao,

        @Schema(description = "ID do evento ao qual este pedido pertence (opcional). " +
                "Se enviado, o pedido é agrupado sob o evento; caso contrário, é um pedido avulso.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", nullable = true)
        UUID eventoId
) {}
