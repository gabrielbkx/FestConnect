package com.gabriel.party.model.agenda;

import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Um período em que o prestador NÃO está disponível, marcado por ele mesmo (evento fechado fora da
 * plataforma, férias, etc.). Ocupa a agenda como um evento confirmado: entra na agenda pública (#87)
 * e na checagem de conflito (#88).
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_bloqueio_agenda")
public class BloqueioAgenda {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fim", nullable = false)
    private LocalDateTime fim;

    @Column(name = "motivo")
    private String motivo;

    @CreationTimestamp
    @Column(name = "data_hora_criacao", nullable = false, updatable = false)
    private LocalDateTime dataHoraCriacao;
}
