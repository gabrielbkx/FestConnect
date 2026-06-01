package com.gabriel.party.repositories.avaliacao;

import com.gabriel.party.model.avaliacao.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Page<Avaliacao> findAllByAtivoTrue(Pageable pageable);

    Optional<Avaliacao> findByIdAndAtivoTrue(UUID id);

    Page<Avaliacao> findAllByPrestadorIdAndAtivoTrue(UUID prestadorId, Pageable pageable);

    boolean existsByPedidoId(UUID pedidoId);

    List<Avaliacao> findAllByPedidoItemCatalogoIdAndAtivoTrue(UUID itemCatalogoId);

    @Query("SELECT ic.id AS idItem, AVG(a.nota) AS media, COUNT(a) AS total " +
           "FROM Avaliacao a JOIN a.pedido p JOIN p.itemCatalogo ic " +
           "WHERE ic.id IN :idsItens AND a.ativo = true GROUP BY ic.id")
    List<EstatisticasItemAvaliacao> buscarEstatisticasPorItens(@Param("idsItens") List<UUID> idsItens);
}
