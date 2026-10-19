package com.gabriel.party.controllers.pagamento;

import com.gabriel.party.dtos.pagamento.PagamentoResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.pagamento.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagamentos")
@Tag(name = "Pagamentos", description = "Pagamento do sinal (arras) que reserva a data do evento.")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento processado; com o sinal aprovado o pedido vai para RESERVADO"),
            @ApiResponse(responseCode = "400", description = "Pedido não está aguardando o sinal ou o prazo expirou"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence a este cliente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "A agenda do prestador encheu durante o pagamento")
    })
    @Operation(summary = "Pagar o sinal", description = "O cliente paga o sinal de um pedido AGUARDANDO_ENTRADA. Aprovado o pagamento, a data é reservada (RESERVADO) e os pedidos concorrentes no mesmo horário são encerrados.")
    @PostMapping("/sinal/{pedidoId}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<PagamentoResponseDTO> pagarSinal(@PathVariable UUID pedidoId,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagamentoService.pagarSinal(pedidoId, usuario));
    }
}
