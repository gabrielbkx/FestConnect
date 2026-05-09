package com.gabriel.party.model.itemcatalogo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_produto")
@DiscriminatorValue("PRODUTO")
@NoArgsConstructor
public class Produto extends ItemCatalogo {}
