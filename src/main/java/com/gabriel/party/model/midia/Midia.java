package com.gabriel.party.model.midia;

import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.midia.enums.TipoMidia;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tb_midia")
public class Midia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "midia_url")
    private String url;

    @Column(name = "tipo_midia")
    @Enumerated(EnumType.STRING)
    private TipoMidia tipo;

    @Column(name = "ordem_exibicao")
    private Integer ordem;

    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "item_catalogo_id", nullable = false)
    private ItemCatalogo itemCatalogo;
}
