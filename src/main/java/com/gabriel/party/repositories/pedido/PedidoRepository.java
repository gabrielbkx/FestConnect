package com.gabriel.party.repositories.pedido;


import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);

    List<Pedido> findByClienteIdOrderByDataHoraCriacaoDesc(UUID clienteId);

    List<Pedido> findByPrestadorIdOrderByDataHoraCriacaoDesc(UUID prestadorId);

    boolean existsByPrestadorIdAndDataEventoAndStatusPedido(UUID prestadorId, LocalDateTime dataEvento, StatusPedido status);

    boolean existsByPrestadorIdAndClienteIdAndStatusPedido(UUID prestadorId, UUID clienteId, StatusPedido status);

    List<Pedido> findByStatusPedidoAndValidadeOrcamentoBefore(StatusPedido status, LocalDateTime agora);

    List<Pedido> findByStatusPedidoAndPrazoRespostaBefore(StatusPedido status, LocalDateTime agora);

    Long countByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);
}