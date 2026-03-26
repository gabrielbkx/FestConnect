package com.gabriel.party.repositories.itemcatalogo;

import com.gabriel.party.model.catalogo.ItemCatalogo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemCatalogoRepository extends JpaRepository<ItemCatalogo, UUID> {

    List<ItemCatalogo> findAllByPrestadorIdAndAtivoTrue(UUID prestadorId);

    Page<ItemCatalogo> findAllByAtivoTrue(Pageable pageable);

    Optional<ItemCatalogo> findByIdAndAtivoTrue(UUID id);
}
