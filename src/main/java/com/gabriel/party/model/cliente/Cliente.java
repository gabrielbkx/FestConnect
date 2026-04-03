package com.gabriel.party.model.cliente;

import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.prestador.endereco.Endereco;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "whatsapp")
    private String whatsapp;

    @Embedded
    private Endereco endereco;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @ManyToMany
    @JoinTable(
            name = "tb_cliente_prestador_favorito",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "prestador_id")
    )
    private Set<Prestador> prestadoresFavoritos;

    @ManyToMany
    @JoinTable(
            name = "tb_cliente_categoria_favorita",
            joinColumns = @JoinColumn(name = "cliente_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categoriasFavoritas;
}

