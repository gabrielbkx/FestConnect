package com.gabriel.party.dtos.pagamento;

import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import com.gabriel.party.model.pagamento.enums.TipoPagamento;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Resultado de um pagamento e o status em que o pedido ficou")
public record PagamentoResponseDTO(

        @Schema(description = "ID do pagamento registrado")
        UUID pagamentoId,

        @Schema(description = "O que foi pago", example = "sinal")
        TipoPagamento tipo,

        @Schema(description = "Valor cobrado em R$", example = "750.00")
        BigDecimal valor,

        @Schema(description = "Situação do pagamento no gateway", example = "aprovado")
        StatusPagamento statusPagamento,

        @Schema(description = "Link do comprovante/checkout, quando houver")
        String urlComprovante,

        @Schema(description = "ID do pedido pago")
        UUID pedidoId,

        @Schema(description = "Status em que o pedido ficou após o pagamento. Com o sinal aprovado, vai para RESERVADO.", example = "reservado")
        StatusPedido statusPedido
) {}
