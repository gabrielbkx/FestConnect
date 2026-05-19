package com.gabriel.party.repositories.evento;

import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.evento.enums.StatusEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoRepository extends JpaRepository<Evento, UUID> {

    List<Evento> findByClienteIdOrderByDataEventoDesc(UUID clienteId);

    List<Evento> findByClienteIdAndStatusOrderByDataEventoDesc(UUID clienteId, StatusEvento status);
}
