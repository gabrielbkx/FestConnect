package com.gabriel.party.repositories.pedido;


import com.gabriel.party.dtos.pedido.IntervaloOcupadoDTO;
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
            SELECT p FROM Pedido p
             WHERE p.prestador.id = :prestadorId
               AND p.statusPedido = :status
               AND p.dataEvento < :fim
               AND :inicio < p.dataFim
             ORDER BY p.dataEvento
            """)
    List<Pedido> buscarSobreposicoes(@Param("prestadorId") UUID prestadorId,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim,
                                     @Param("status") StatusPedido status);

    /**
     * Intervalos RESERVADO do prestador que tocam a janela [de, ate]. Só os horários, nada do cliente:
     * é o que alimenta a agenda pública (ver quando está ocupado, nunca com quem).
     */
    /**
     * Pedidos do prestador em algum dos status dados que se sobrepõem a [inicio, fim), exceto um
     * (o que acabou de reservar). Usado para fechar os concorrentes quando a data é reservada (#89).
     */
    @Query("""
            SELECT p FROM Pedido p
             WHERE p.prestador.id = :prestadorId
               AND p.statusPedido IN :statuses
               AND p.id <> :exceto
               AND p.dataEvento < :fim
               AND :inicio < p.dataFim
             ORDER BY p.dataEvento
            """)
    List<Pedido> buscarSobreposicoesAtivas(@Param("prestadorId") UUID prestadorId,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim,
                                           @Param("statuses") List<StatusPedido> statuses,
                                           @Param("exceto") UUID exceto);

    @Query("""
            SELECT new com.gabriel.party.dtos.pedido.IntervaloOcupadoDTO(p.dataEvento, p.dataFim)
              FROM Pedido p
             WHERE p.prestador.id = :prestadorId
               AND p.statusPedido = com.gabriel.party.model.pedido.enums.StatusPedido.RESERVADO
               AND p.dataEvento < :ate
               AND :de < p.dataFim
             ORDER BY p.dataEvento
            """)
    List<IntervaloOcupadoDTO> agendaOcupada(@Param("prestadorId") UUID prestadorId,
                                            @Param("de") LocalDateTime de,
                                            @Param("ate") LocalDateTime ate);

    boolean existsByPrestadorIdAndClienteIdAndStatusPedido(UUID prestadorId, UUID clienteId, StatusPedido status);

    List<Pedido> findByStatusPedidoAndValidadeOrcamentoBefore(StatusPedido status, LocalDateTime agora);

    List<Pedido> findByStatusPedidoAndPrazoRespostaBefore(StatusPedido status, LocalDateTime agora);

    List<Pedido> findByStatusPedidoAndPrazoEntradaBefore(StatusPedido status, LocalDateTime agora);

    List<Pedido> findByStatusPedidoAndDataFimBefore(StatusPedido status, LocalDateTime agora);

    Long countByPrestadorIdAndStatusPedido(UUID prestadorId, StatusPedido status);

    boolean existsByPrestadorIdAndItemCatalogoIdAndEventoIdAndStatusPedidoIn(
            UUID prestadorId, UUID itemCatalogoId, UUID eventoId, List<StatusPedido> statuses);

    List<Pedido> findByEventoIdAndStatusPedidoIn(UUID eventoId, List<StatusPedido> statuses);
}