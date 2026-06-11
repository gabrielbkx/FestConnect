package com.gabriel.party.dtos.pedido;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrcamentoLoteItemDTO(
        @NotNull UUID prestadorId,
        @NotNull UUID itemCatalogoId
) {}
