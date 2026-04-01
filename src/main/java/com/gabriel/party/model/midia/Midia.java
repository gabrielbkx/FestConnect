package com.gabriel.party.model.midia;

import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.midia.enums.TipoMidia;
import com.gabriel.party.model.prestador.Prestador;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    @ManyToOne
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;


    @ManyToOne
    @JoinColumn(name = "item_catalogo_id")
    private ItemCatalogo itemCatalogo;
}
