package com.gabriel.party.dtos.itemcatalogo;

import com.gabriel.party.dtos.midia.MidiaResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ItemCatalogoResponseDTO(
        UUID id,
        String titulo,
        String descricao,
        BigDecimal precoBase,
        String tipo,
        boolean ativo,
        List<MidiaResponseDTO> midias,
        LocalDetalheDTO localDetalhe
) {}