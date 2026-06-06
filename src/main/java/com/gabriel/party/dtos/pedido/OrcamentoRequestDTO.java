package com.gabriel.party.dtos.pedido;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;


@Schema(description = "Dados do orçamento enviado pelo prestador em resposta a um pedido")
public record OrcamentoRequestDTO(

        @Schema(description = "Valor total do orçamento em R$", example = "2500.00")
        @NotNull @Positive
        BigDecimal valor,

        @Schema(description = "Descrição detalhada do que está incluído no orçamento", example = "Inclui: buffet completo para 150 pessoas, garçons, limpeza pós-evento. Não inclui: bebidas alcoólicas.")
        @NotBlank
        String detalhesOrcamento

) {}
