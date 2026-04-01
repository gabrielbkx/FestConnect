package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.model.itemcatalogo.enums.TipoItem;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemCatalogoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        BigDecimal precoBase,
        TipoItem tipo,
        boolean ativo
        // Mais para frente, vou adicionar a lista de URLs de Mídia aqui dentro
) {}