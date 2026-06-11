package com.gabriel.party.dtos.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrcamentoLoteRequestDTO(
        @NotEmpty
        @Size(max = 30)
        @Valid
        List<OrcamentoLoteItemDTO> itens
) {}
