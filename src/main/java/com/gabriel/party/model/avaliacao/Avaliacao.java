package com.gabriel.party.model.avaliacao;

import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nota", nullable = false)
    private Integer nota;
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "prestador_id")
    @Column(name = "prestador_id", nullable = false)
    private Prestador prestador;
}
