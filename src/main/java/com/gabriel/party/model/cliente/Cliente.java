package com.gabriel.party.model.cliente;

import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.shared.Endereco;
import com.gabriel.party.model.usuario.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_cliente")
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nomeCompleto;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Embedded
    private Endereco endereco;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "tb_cliente_prestador_favorito",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "prestador_id")
    )
    private Set<Prestador> prestadoresFavoritos;

    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(
            name = "tb_cliente_categoria_favorita",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categoriasFavoritas;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id_usuario", unique = true)
    private Usuario usuario;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Set<Avaliacao> avaliacoesFeitas;
}

