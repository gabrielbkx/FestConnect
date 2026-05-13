package com.gabriel.party.model.prestador;


import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.shared.Endereco;
import com.gabriel.party.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;
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

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "cnpj_ou_cpf", unique = true, nullable = false)
    private String cnpjOuCpf;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "whatsapp", nullable = true)
    private String whatsapp;

    @Column(name = "foto_perfil_url", nullable = true)
    private String fotoPerfilUrl;

    @Embedded
    private Endereco endereco;

    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private List<Midia> midias;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private Set<Avaliacao> avaliacoes;

    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCatalogo> itensCatalogo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id_usuario", unique = true)
    private Usuario usuario;

}
