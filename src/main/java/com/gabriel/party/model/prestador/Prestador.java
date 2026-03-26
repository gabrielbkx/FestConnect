package com.gabriel.party.model.prestador;


import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.midia.Midia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_prestador")
public class Prestador {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome_negocio", nullable = false)
    private String nomeNegocio;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String whatsapp;

    // Localização simplificada para o início do MVP
    private Double latitude;
    private Double longitude;

    @Column(name = "raio_atendimento_km")
    private Double raioAtendimentoKm;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private List<Midia> midias;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;
}
