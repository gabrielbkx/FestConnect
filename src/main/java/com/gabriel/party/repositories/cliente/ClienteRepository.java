package com.gabriel.party.repositories.cliente;

import com.gabriel.party.model.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    boolean existsByEmail(String email);
}

