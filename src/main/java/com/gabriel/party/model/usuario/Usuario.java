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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Column(name = "email_verificado")
    private Boolean emailVerificado = false;

    @Column(name = "token_verificacao")
    private String  tokenVerificacao;

    @Column(name = "token_expiracao")
    private LocalDateTime tokenExpiracao;

    /**
     * Tokens de login emitidos ANTES deste momento não valem mais. É o "estado" mínimo que o
     * servidor guarda para ter logout com JWT, que por natureza é stateless. Nulo = nenhuma sessão encerrada.
     * Instant (e não LocalDateTime) porque é comparado com o "iat" do token, que é um instante em UTC.
     */
    @Column(name = "sessoes_validas_desde")
    private Instant sessoesValidasDesde;

    /**
     * Derruba todos os tokens emitidos até agora (sair de todos os dispositivos, troca de senha).
     * <p>
     * O "iat" do JWT só tem precisão de segundos, então arredondamos PARA CIMA, para o próximo segundo
     * cheio: assim todo token emitido até agora, inclusive neste mesmo segundo, deixa de valer.
     * Arredondar para baixo deixaria escapar um token emitido no mesmo segundo, antes do encerramento.
     * Efeito colateral aceito: um login feito no mesmo segundo, logo depois, também cai; pela tela
     * isso não acontece, porque a pessoa precisa digitar a senha de novo.
     */
    public void encerrarTodasAsSessoes() {
        this.sessoesValidasDesde = Instant.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);
    }

    @Override
    public boolean isEnabled() {
        return this.ativo && Boolean.TRUE.equals(this.emailVerificado);
    }
}
