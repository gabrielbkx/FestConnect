package com.gabriel.party.dtos.pedido;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoRequestDTO(

        @NotNull(message = "O ID do prestador é obrigatório")
        UUID prestadorId,

        @NotNull(message = "A data do evento é obrigatória")
        @Future(message = "A data do evento deve ser no futuro")
        LocalDateTime dataEvento,

        @NotBlank(message = "O local do evento é obrigatório")
        String localEvento,

        @NotBlank(message = "O tipo de evento é obrigatório")
        String tipoEvento,

        @NotNull(message = "O número de convidados é obrigatório")
        @Min(value = 1, message = "O evento deve ter pelo menos 1 convidado")
        Integer numeroConvidados,

        @NotBlank(message = "A descrição do pedido é obrigatória")
        String descricao
) {}