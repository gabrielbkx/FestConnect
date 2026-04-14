package com.gabriel.party.dtos.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;

import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        String nomeCliente,
        String fotoClienteUrl,
        LocalDateTime dataEvento,
        String localEvento,
        String tipoEvento,
        Integer numeroConvidados,
        String descricao,
        StatusPedido status
) {}