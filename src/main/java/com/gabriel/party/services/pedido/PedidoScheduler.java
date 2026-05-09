package com.gabriel.party.services.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PedidoScheduler {

    private final PedidoRepository pedidoRepository;

    public PedidoScheduler(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Scheduled(fixedRate = 3_600_000) // roda a cada 1 hora
    @Transactional
    public void expirarOrcamentosVencidos() {
        int expirados = pedidoRepository.expirarOrcamentosVencidos(
                StatusPedido.ORCADO, StatusPedido.EXPIRADO, LocalDateTime.now());
        if (expirados > 0) {
            System.out.printf("[PedidoScheduler] %d orçamento(s) expirado(s)%n", expirados);
        }
    }
}
