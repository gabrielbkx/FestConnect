package com.gabriel.party.repositories.pedido;


import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);

    List<Pedido> findByClienteIdOrderByDataHoraCriacaoDesc(UUID clienteId);

    List<Pedido> findByPrestadorIdOrderByDataHoraCriacaoDesc(UUID prestadorId);

    /**
     * O prestador já tem algum pedido no status dado que se SOBREPÕE ao intervalo [inicio, fim)?
     * Sobreposição de intervalos semiabertos: inicioA &lt; fimB AND inicioB &lt; fimA. Encostado
     * (fim == inicio) não conflita. Esta é a checagem na aplicação; a garantia contra corrida
     * fica na restrição de exclusão do banco (#85).
     */
    @Query("""
            SELECT COUNT(p) > 0 FROM Pedido p
             WHERE p.prestador.id = :prestadorId
               AND p.statusPedido = :status
               AND p.dataEvento < :fim
               AND :inicio < p.dataFim
            """)
    boolean existsSobreposicao(@Param("prestadorId") UUID prestadorId,
                               @Param("inicio") LocalDateTime inicio,
                               @Param("fim") LocalDateTime fim,
                               @Param("status") StatusPedido status);

    boolean existsByPrestadorIdAndClienteIdAndStatusPedido(UUID prestadorId, UUID clienteId, StatusPedido status);

    List<Pedido> findByStatusPedidoAndValidadeOrcamentoBefore(StatusPedido status, LocalDateTime agora);

    List<Pedido> findByStatusPedidoAndPrazoRespostaBefore(StatusPedido status, LocalDateTime agora);

    Long countByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);

    boolean existsByPrestadorIdAndItemCatalogoIdAndEventoIdAndStatusPedidoIn(
            UUID prestadorId, UUID itemCatalogoId, UUID eventoId, List<StatusPedido> statuses);

    List<Pedido> findByEventoIdAndStatusPedidoIn(UUID eventoId, List<StatusPedido> statuses);
}