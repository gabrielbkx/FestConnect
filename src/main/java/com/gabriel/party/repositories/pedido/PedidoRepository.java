package com.gabriel.party.repositories.pedido;


import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);



}
