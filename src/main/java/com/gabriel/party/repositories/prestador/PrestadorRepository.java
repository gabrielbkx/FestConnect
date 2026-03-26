package com.gabriel.party.repositories.prestador;

import com.gabriel.party.model.prestador.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PrestadorRepository extends JpaRepository<Prestador, UUID> {
}
