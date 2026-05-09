package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemCatalogoRequestDTO(

        @NotBlank(message = "O título do anúncio é obrigatório")
        String titulo,

        String descricao,

        BigDecimal precoBase,

        @NotNull(message = "O tipo do item (PRODUTO, SERVICO ou LOCAL) é obrigatório")
        TipoItem tipo,

        LocalDetalheDTO localDetalhe

) {}