package com.gabriel.party.model.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_item_catalogo")
public class ItemCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_base")
    private BigDecimal precoBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoItem tipo;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Relação com o dono do anúncio
    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;


    @OneToMany(mappedBy = "itemCatalogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Midia> midias;
}