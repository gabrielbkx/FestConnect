package com.gabriel.party.dtos.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Dados completos de um pedido")
public record PedidoResponseDTO(

        @Schema(description = "ID único do pedido")
        UUID id,

        @Schema(description = "Nome completo do cliente", example = "João da Silva")
        String nomeCliente,

        @Schema(description = "URL da foto de perfil do cliente")
        String fotoClienteUrl,

        @Schema(description = "ID do prestador vinculado ao pedido")
        UUID prestadorId,

        @Schema(description = "Nome do prestador vinculado ao pedido", example = "Buffet Silva & Cia")
        String nomePrestador,

        @Schema(description = "WhatsApp do prestador (disponível após aceite)", example = "11987654321")
        String whatsappPrestador,

        @Schema(description = "Data e hora do evento")
        LocalDateTime dataEvento,

        @Schema(description = "Local onde o evento ocorrerá", example = "Salão de Festas do Condomínio Primavera")
        String localEvento,

        @Schema(description = "Tipo ou tema do evento", example = "Casamento")
        String tipoEvento,

        @Schema(description = "Número de convidados", example = "150")
        Integer numeroConvidados,

        @Schema(description = "Descrição do que o cliente precisa")
        String descricao,

        @Schema(description = "Status atual do pedido. Fluxo: PENDENTE → ORCADO → ACEITO. " +
                "O prestador pode RECUSAR a qualquer momento; o cliente pode CANCELAR. " +
                "O sistema EXPIRA orçamentos vencidos.",
                allowableValues = {"PENDENTE", "ORCADO", "ACEITO", "RECUSADO", "CANCELADO", "EXPIRADO"})
        StatusPedido status,

        @Schema(description = "Valor do orçamento em R$. Preenchido após o prestador enviar o orçamento.", example = "2500.00")
        BigDecimal valor,

        @Schema(description = "Descrição do que está incluído no orçamento. Preenchido após o prestador enviar o orçamento.")
        String detalhesOrcamento,

        @Schema(description = "Data limite para aceitar o orçamento. Após esta data o pedido é automaticamente EXPIRADO.")
        LocalDateTime validadeOrcamento,

        @Schema(description = "Data e hora de criação do pedido")
        LocalDateTime dataHoraCriacao
) {}
