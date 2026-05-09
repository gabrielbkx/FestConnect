package com.gabriel.party.model.itemcatalogo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_servico")
@DiscriminatorValue("SERVICO")
@NoArgsConstructor
public class Servico extends ItemCatalogo {}
