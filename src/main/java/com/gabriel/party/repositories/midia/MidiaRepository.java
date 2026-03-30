package com.gabriel.party.repositories.midia;

import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.midia.enums.TipoMidia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MidiaRepository extends JpaRepository<Midia, UUID> {

    @Query(
            value = "SELECT m FROM Midia m" +
                    " WHERE m.prestador.id = :prestadorId " +
                    "AND (:tipoMidia IS NULL OR m.tipo = :tipoMidia)",
            countQuery = "SELECT COUNT(m) FROM Midia m " +
                    "WHERE m.prestador.id = :prestadorId " +
                    "AND (:tipoMidia IS NULL OR m.tipo = :tipoMidia)"
    )
    Page<Midia> buscarTodasAsMidiasDoPrestador(
            Pageable pageable,
            @Param("prestadorId") UUID prestadorId,
            @Param("tipoMidia") TipoMidia tipoMidia
    );


    Optional<Midia> findById(UUID id);
}
