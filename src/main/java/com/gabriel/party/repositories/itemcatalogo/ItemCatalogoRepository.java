package com.gabriel.party.repositories.itemcatalogo;

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

    boolean existsByCategoriaIdAndAtivoTrue(UUID categoriaId);


    @Query(value = """
    SELECT i.* FROM tb_item_catalogo i
    INNER JOIN tb_prestador p ON i.prestador_id = p.id
    WHERE p.ativo = true
      AND i.ativo = true
      AND (:termoBusca = '' OR LOWER(i.titulo) LIKE LOWER(CONCAT('%', :termoBusca, '%')) OR LOWER(i.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%')))
      AND (:tipo = '' OR i.tipo = :tipo)
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
      AND i.ativo = true
      AND (:termoBusca = '' OR LOWER(i.titulo) LIKE LOWER(CONCAT('%', :termoBusca, '%')) OR LOWER(i.descricao) LIKE LOWER(CONCAT('%', :termoBusca, '%')))
      AND (:tipo = '' OR i.tipo = :tipo)
      AND (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(p.latitude)) * cos(radians(p.longitude) - radians(:lonCliente)) +
          sin(radians(:latCliente)) * sin(radians(p.latitude))
      )) <= :raioKm
    """,
            nativeQuery = true)
    Page<ItemCatalogo> buscarItensPorTermoEProximidade(
            @Param("termoBusca") String termoBusca,
            @Param("tipo") String tipo,
            @Param("latCliente") Double lat,
            @Param("lonCliente") Double lon,
            @Param("raioKm") Double raio,
            Pageable pageable
    );
}
