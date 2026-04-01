package com.gabriel.party.model.prestador;


import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.prestador.endereco.Endereco;
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

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "whatsapp", nullable = true)
    private String whatsapp;

    @Embedded
    private Endereco endereco;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private List<Midia> midias;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private List<Avaliacao> avaliacoes;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCatalogo> itensCatalogo;

}
