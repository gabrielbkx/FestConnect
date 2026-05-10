package com.gabriel.party.controllers.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.pedido.PedidoService;
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
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> solicitarOrcamento(
            @RequestBody @Valid PedidoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        PedidoResponseDTO pedidoCriado = pedidoService.criarPedido(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCriado);
    }

    @GetMapping("/cliente")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidosDoCliente(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPedidosDoCliente(usuario));
    }

    @GetMapping("/prestador/pendentes")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<List<PedidoResponseDTO>> listarPendentes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPedidosPendentes(usuario));
    }

    @GetMapping("/prestador/historico")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<List<PedidoResponseDTO>> listarHistoricoPrestador(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarTodosPedidosDoPrestador(usuario));
    }

    @PutMapping("/{id}/orcar")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<PedidoResponseDTO> orcar(@PathVariable UUID id,
                                                   @RequestBody @Valid OrcamentoRequestDTO dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.enviarOrcamento(id, dto, usuario));
    }

    @PutMapping("/{id}/recusar")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<Void> recusar(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        pedidoService.recusarPedido(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/aceitar")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<PedidoResponseDTO> aceitar(@PathVariable UUID id,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.aceitarOrcamento(id, usuario));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id,
                                         @AuthenticationPrincipal Usuario usuario) {
        pedidoService.cancelarPedidoPeloCliente(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
