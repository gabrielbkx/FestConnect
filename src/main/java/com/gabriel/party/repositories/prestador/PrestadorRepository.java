package com.gabriel.party.repositories.prestador;

import com.gabriel.party.model.prestador.Prestador;
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
public interface PrestadorRepository extends JpaRepository<Prestador, UUID> {

    Page<Prestador> findAllByAtivoTrue(Pageable pageable);

    Optional<Prestador> findByIdAndAtivoTrue(UUID id);

    @Query(value = """

            SELECT * FROM tb_prestador
    WHERE (6371 * acos(
        cos(radians(:latCliente)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lonCliente)) + 
        sin(radians(:latCliente)) * sin(radians(latitude))
    )) <= :raioKm
    ORDER BY (6371 * acos(
        cos(radians(:latCliente)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lonCliente)) + 
        sin(radians(:latCliente)) * sin(radians(latitude))
    )) ASC
    """, nativeQuery = true)
    List<Prestador> buscarPorProximidade(
            @Param("latCliente") Double lat,
            @Param("lonCliente") Double lon,
            @Param("raioKm") Double raio
    );

    @Query(value = """
    SELECT prestador.* FROM tb_prestador prestador
    WHERE prestador.categoria_id = :categoriaId
      AND prestador.ativo = true
      AND (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(prestador.latitude)) * cos(radians(prestador.longitude) - radians(:lonCliente)) + 
          sin(radians(:latCliente)) * sin(radians(prestador.latitude))
      )) <= :raioKm
    ORDER BY (6371 * acos(
          cos(radians(:latCliente)) * cos(radians(prestador.latitude)) * cos(radians(prestador.longitude) - radians(:lonCliente)) + 
          sin(radians(:latCliente)) * sin(radians(prestador.latitude))
    )) ASC
    """, nativeQuery = true)
    List<Prestador> buscarPorCategoriaEProximidade(
            @Param("categoriaId") UUID categoriaId,
            @Param("latCliente") Double lat,
            @Param("lonCliente") Double lon,
            @Param("raioKm") Double raio
    );

    boolean existsByCnpjOuCpf(String cpfOuCnpj);
}