package com.gabriel.party.model.usuario;

import com.gabriel.party.model.usuario.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Table(name = "tb_usuarios")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    private UUID id;
    @Column(name = "email", unique = true,nullable = false)
    private String email;
    @Column(name = "senha",nullable = false)
    private String senha;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == Role.ROLE_ADMINISTRADOR) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
        } else if (this.role == Role.ROLE_PRESTADOR) {
            return List.of(new SimpleGrantedAuthority("ROLE_PRESTADOR"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")); // O padrão será cliente
        }
    }

    @Override
    public @Nullable String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
          return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }
}
