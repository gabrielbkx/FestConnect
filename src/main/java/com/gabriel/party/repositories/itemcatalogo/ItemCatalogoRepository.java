package com.gabriel.party.repositories.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
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
public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, UUID> {

    List<ItemCatalogo> findAllByPrestadorIdAndAtivoTrue(UUID prestadorId);

    Page<ItemCatalogo> findAllByAtivoTrue(Pageable pageable);

    Optional<ItemCatalogo> findByIdAndAtivoTrue(UUID id);


    @Query(value = """
    SELECT i.* FROM tb_item_catalogo i
    INNER JOIN tb_prestador p ON i.prestador_id = p.id
    WHERE p.ativo = true
      AND (:termoBusca = '' OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%')) OR LOWER(i.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%')))
      AND (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lonCliente)) + 
          sin(radians(:latCliente)) * sin(radians(p.latitude))
      )) <= :raioKm
    ORDER BY (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lonCliente)) + 
          sin(radians(:latCliente)) * sin(radians(p.latitude))
    )) ASC
    """,
            countQuery = """
    SELECT count(i.id) FROM tb_item_catalogo i
    INNER JOIN tb_prestador p ON i.prestador_id = p.id
    WHERE p.ativo = true
      AND (:termoBusca = '' OR LOWER(i.nome) LIKE LOWER(CONCAT('%', :termoBusca, '%')) OR LOWER(i.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%')))
      AND (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lonCliente)) + 
          sin(radians(:latCliente)) * sin(radians(p.latitude))
      )) <= :raioKm
    """,
            nativeQuery = true)
    Page<ItemCatalogoResponseDTO> buscarItensPorTermoEProximidade(
            @Param("termoBusca") String termoBusca,
            @Param("latCliente") Double lat,
            @Param("lonCliente") Double lon,
            @Param("raioKm") Double raio,
            Pageable pageable
    );
}
