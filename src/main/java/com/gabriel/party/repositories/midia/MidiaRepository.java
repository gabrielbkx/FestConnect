package com.gabriel.party.repositories.midia;

import com.gabriel.party.model.midia.Midia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MidiaRepository extends JpaRepository<Midia, UUID> {
}
