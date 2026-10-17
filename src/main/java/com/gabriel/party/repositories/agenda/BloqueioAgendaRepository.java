package com.gabriel.party.repositories.agenda;

import com.gabriel.party.dtos.pedido.IntervaloOcupadoDTO;
import com.gabriel.party.model.agenda.BloqueioAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BloqueioAgendaRepository extends JpaRepository<BloqueioAgenda, UUID> {

    List<BloqueioAgenda> findByPrestadorIdOrderByInicio(UUID prestadorId);

    /** Bloqueios do prestador que se sobrepõem a [inicio, fim). Alimentam a checagem de conflito (#88). */
    @Query("""
            SELECT b FROM BloqueioAgenda b
             WHERE b.prestador.id = :prestadorId
               AND b.inicio < :fim
               AND :inicio < b.fim
            """)
    List<BloqueioAgenda> buscarSobreposicoes(@Param("prestadorId") UUID prestadorId,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);

    /** Bloqueios do prestador na janela [de, ate], como intervalos ocupados para a agenda pública (#87). */
    @Query("""
            SELECT new com.gabriel.party.dtos.pedido.IntervaloOcupadoDTO(b.inicio, b.fim)
              FROM BloqueioAgenda b
             WHERE b.prestador.id = :prestadorId
               AND b.inicio < :ate
               AND :de < b.fim
             ORDER BY b.inicio
            """)
    List<IntervaloOcupadoDTO> agendaOcupada(@Param("prestadorId") UUID prestadorId,
                                            @Param("de") LocalDateTime de,
                                            @Param("ate") LocalDateTime ate);
}
