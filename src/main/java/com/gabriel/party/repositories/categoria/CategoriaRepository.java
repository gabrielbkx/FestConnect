package com.gabriel.party.repositories.categoria;

import com.gabriel.party.dtos.categoria.CategoriaRequestDTO;
import com.gabriel.party.model.categoria.Categoria;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    Page<Categoria> findAllByAtivoTrue(Pageable pageable);

    Optional<Categoria> findByIdAndAtivoTrue(UUID id);

    Optional<Categoria> findByNomeIgnoreCaseAndAtivoTrue(String nome);
}
