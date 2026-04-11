package com.gabriel.party.model.autenticacao;

import com.gabriel.party.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_codigo_recuperacao")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CodigoRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_codigo_recuperacao", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "codigo_pin", nullable = false,length = 6)
    String codigoPin;

    @Column(name = "data_expiracao", nullable = false)
    LocalDateTime dataExpiracao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    Usuario usuario;
}
