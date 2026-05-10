package com.gabriel.party.dtos.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        String nomeCliente,
        String fotoClienteUrl,
        String nomePrestador,
        LocalDateTime dataEvento,
        String localEvento,
        String tipoEvento,
        Integer numeroConvidados,
        String descricao,
        StatusPedido status,
        BigDecimal valor,
        String detalhesOrcamento,
        LocalDateTime validadeOrcamento,
        LocalDateTime dataHoraCriacao
) {}
