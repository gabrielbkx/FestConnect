package com.gabriel.party.model.itemcatalogo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_local")
@DiscriminatorValue("LOCAL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Local extends ItemCatalogo {

    @Column(name = "capacidade_maxima")
    private Integer capacidadeMaxima;

    @Column(name = "metragem", precision = 10, scale = 2)
    private BigDecimal metragem;

    @Column(name = "permite_som")
    private Boolean permiteSom;

    @Column(name = "tem_estacionamento")
    private Boolean temEstacionamento;

    @Column(name = "tipo_espaco", length = 50)
    private String tipoEspaco;
}
