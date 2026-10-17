package com.gabriel.party.dtos.pedido;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Um intervalo em que o prestador já está comprometido. De propósito NÃO traz nenhum dado do
 * cliente nem do pedido: quem consulta a agenda pública vê que está ocupado, nunca com quem.
 */
@Schema(description = "Intervalo ocupado na agenda do prestador (sem nenhum dado do cliente)")
public record IntervaloOcupadoDTO(

        @Schema(description = "Início do compromisso")
        LocalDateTime inicio,

        @Schema(description = "Fim do compromisso")
        LocalDateTime fim
) {}
