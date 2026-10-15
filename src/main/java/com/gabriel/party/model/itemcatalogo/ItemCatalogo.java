package com.gabriel.party.model.itemcatalogo;

import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
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
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class ItemCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "preco_base",precision = 10, scale = 2, nullable = false)
    private BigDecimal precoBase;

    // Quanto tempo o serviço ocupa a agenda do prestador. É o que dá "fim" ao pedido e permite
    // detectar sobreposição de horários (ver #85). Itens anteriores receberam 240 na migration.
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "itemCatalogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Midia> midias;
}