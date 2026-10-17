package com.gabriel.party.model.prestador;


import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.shared.Endereco;
import com.gabriel.party.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    @Column(name = "foto_banner_url", nullable = true)
    private String fotoBannerUrl;

    @Embedded
    private Endereco endereco;

    // Quantos eventos o prestador atende ao mesmo tempo (buffet com 2 equipes = 2). Com valor > 1 a
    // garantia deixa de ser a restrição de exclusão do banco e vira contagem sob lock (#88).
    @Column(name = "eventos_simultaneos", nullable = false)
    private Integer eventosSimultaneos = 1;

    // Minutos de folga (montagem/deslocamento) antes e depois de cada evento. A ocupação real do
    // prestador é [inicio - folga, fim + folga). Entra na checagem de conflito e na agenda.
    @Column(name = "folga_minutos", nullable = false)
    private Integer folgaMinutos = 0;

    private Boolean ativo = true;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL)
    private Set<Avaliacao> avaliacoes;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCatalogo> itensCatalogo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id_usuario", unique = true)
    private Usuario usuario;

}
