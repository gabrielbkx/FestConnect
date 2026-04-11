package com.gabriel.party.repositories.autenticacao;

import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoRecuperacaoRepository extends JpaRepository<CodigoRecuperacao, UUID > {


    Optional<CodigoRecuperacao> findByUsuarioEmail(String email);
}
