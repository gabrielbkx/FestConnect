package com.gabriel.party.repositories.Usuario;

import com.gabriel.party.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);


}
