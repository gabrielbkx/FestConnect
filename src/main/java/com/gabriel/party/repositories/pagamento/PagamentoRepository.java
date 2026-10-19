package com.gabriel.party.repositories.pagamento;

import com.gabriel.party.model.pagamento.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {
}
