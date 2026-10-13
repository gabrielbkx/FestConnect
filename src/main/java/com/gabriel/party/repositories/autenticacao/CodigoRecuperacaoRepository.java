package com.gabriel.party.repositories.autenticacao;

import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import com.gabriel.party.model.usuario.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;

@Repository
public interface CodigoRecuperacaoRepository extends JpaRepository<CodigoRecuperacao, UUID > {

    /**
     * Trava a linha do código (SELECT ... FOR UPDATE) até o fim da transação.
     * Sem a trava, várias tentativas em paralelo leem o mesmo contador e o limite
     * de tentativas pode ser furado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CodigoRecuperacao> findByUsuarioEmail(String email);

    /**
     * Delete em lote, executado na hora. Um deleteBy derivado só agenda a remoção,
     * e o Hibernate executa os INSERTs antes dos DELETEs no flush, o que violaria
     * a restrição de um código por usuário ao gravar o código novo.
     */
    @Modifying
    @Query("delete from CodigoRecuperacao c where c.usuario = :usuario")
    void deleteByUsuario(@Param("usuario") Usuario usuario);
}
