package com.gabriel.party.repositories.avaliacao;

import com.gabriel.party.model.avaliacao.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    Page<Avaliacao> findAllByAtivoTrue(Pageable pageable);

    Optional<Avaliacao> findByIdAndAtivoTrue(UUID id);

    Page<Avaliacao> findAllByPrestadorIdAndAtivoTrue(UUID prestadorId, Pageable pageable);

    boolean existsByClienteIdAndPrestadorIdAndAtivoTrue(UUID clienteId, UUID prestadorId);
}
