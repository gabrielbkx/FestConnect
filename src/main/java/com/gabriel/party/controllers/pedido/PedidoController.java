package com.gabriel.party.controllers.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.pedido.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Fluxo de orçamento entre cliente e prestador. " +
        "Ciclo: PENDENTE → ORCADO → ACEITO. O prestador pode RECUSAR; o cliente pode CANCELAR; " +
        "orçamentos vencidos são automaticamente EXPIRADOS.")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido criado e prestador notificado por e-mail"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "Solicitar orçamento", description = "Cria um novo pedido com status PENDENTE e notifica o prestador por e-mail. Requer autenticação como CLIENTE.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> solicitarOrcamento(
            @RequestBody @Valid PedidoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        PedidoResponseDTO pedidoCriado = pedidoService.criarPedido(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCriado);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido retornado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence ao usuário autenticado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @Operation(summary = "Buscar pedido por ID", description = "Retorna os dados de um pedido. Acessível pelo cliente ou prestador vinculado ao pedido.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_PRESTADOR')")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable UUID id,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.buscarPedidoPorId(id, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos do cliente retornada"),
            @ApiResponse(responseCode = "404", description = "Perfil de cliente não encontrado")
    })
    @Operation(summary = "Meus pedidos (cliente)", description = "Retorna todos os pedidos do cliente autenticado, do mais recente ao mais antigo.")
    @GetMapping("/cliente")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosDoCliente(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPedidosDoCliente(usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos pendentes retornada"),
            @ApiResponse(responseCode = "404", description = "Perfil de prestador não encontrado")
    })
    @Operation(summary = "Pedidos pendentes (prestador)", description = "Retorna os pedidos com status PENDENTE aguardando orçamento do prestador autenticado.")
    @GetMapping("/prestador/pendentes")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<List<PedidoResponseDTO>> listarPendentes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPedidosPendentes(usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico de pedidos retornado"),
            @ApiResponse(responseCode = "404", description = "Perfil de prestador não encontrado")
    })
    @Operation(summary = "Histórico completo (prestador)", description = "Retorna todos os pedidos do prestador autenticado em qualquer status, do mais recente ao mais antigo.")
    @GetMapping("/prestador/historico")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<List<PedidoResponseDTO>> listarHistoricoPrestador(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarTodosPedidosDoPrestador(usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orçamento enviado e cliente notificado por e-mail"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence a este prestador"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @Operation(summary = "Enviar orçamento", description = "O prestador responde ao pedido com valor, detalhes e prazo de validade. Status muda para ORCADO.")
    @PutMapping("/{id}/orcar")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<PedidoResponseDTO> orcar(@PathVariable UUID id,
                                                   @RequestBody @Valid OrcamentoRequestDTO dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.enviarOrcamento(id, dto, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido recusado e cliente notificado por e-mail"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence a este prestador"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @Operation(summary = "Recusar pedido", description = "O prestador rejeita o pedido. Status muda para RECUSADO.")
    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<Void> recusar(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        pedidoService.recusarPedido(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orçamento aceito e prestador notificado por e-mail"),
            @ApiResponse(responseCode = "400", description = "Pedido não está com status ORCADO"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence a este cliente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Prestador já tem pedido confirmado nesta data")
    })
    @Operation(summary = "Aceitar orçamento", description = "O cliente aceita o orçamento enviado pelo prestador. Status muda para ACEITO. Exige status atual = ORCADO.")
    @PutMapping("/{id}/aceitar")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> aceitar(@PathVariable UUID id,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.aceitarOrcamento(id, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido cancelado e prestador notificado por e-mail"),
            @ApiResponse(responseCode = "403", description = "Pedido não pertence a este cliente"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    @Operation(summary = "Cancelar pedido", description = "O cliente cancela o pedido. Status muda para CANCELADO.")
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id,
                                         @AuthenticationPrincipal Usuario usuario) {
        pedidoService.cancelarPedidoPeloCliente(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
