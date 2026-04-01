package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemCatalogoRequestDTO(
        @NotBlank(message = "O título do anúncio é obrigatório")
        String titulo,

        String descricao,

        BigDecimal precoBase,

        @NotNull(message = "O tipo do item (PRODUTO ou SERVICO) é obrigatório")
        TipoItem tipo,

        @NotNull(message = "O ID do prestador dono do anúncio é obrigatório")
        UUID prestadorId
) {}