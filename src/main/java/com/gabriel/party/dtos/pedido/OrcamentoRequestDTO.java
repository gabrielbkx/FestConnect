package com.gabriel.party.dtos.pedido;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrcamentoRequestDTO(
        @NotNull @Positive BigDecimal valor,
        @NotBlank String detalhesOrcamento,
        @NotNull @Future LocalDateTime validadeOrcamento
) {}