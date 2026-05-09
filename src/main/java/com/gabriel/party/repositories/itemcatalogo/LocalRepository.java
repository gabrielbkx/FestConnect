package com.gabriel.party.repositories.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocalRepository extends JpaRepository<Local, UUID> {}
