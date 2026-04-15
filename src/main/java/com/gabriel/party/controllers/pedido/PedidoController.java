package com.gabriel.party.controllers.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.pedido.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
public class PedidoController {

    private PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> solicitarOrcamento(
            @RequestBody @Valid PedidoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado) {

        Pedido pedidoCriado = pedidoService.criarPedido(dto, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCriado);
    }

    @GetMapping("/prestador/pendentes")
    public ResponseEntity<List<PedidoResponseDTO>> listarPendentes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.listarPedidosPendentes(usuario));
    }

    @PutMapping("/{id}/orcar")
    public ResponseEntity<PedidoResponseDTO> orcar(@PathVariable UUID id,
                                                   @RequestBody @Valid OrcamentoRequestDTO dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.enviarOrcamento(id, dto, usuario));
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<Void> recusar(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {
        pedidoService.recusarPedido(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/aceitar")
    public ResponseEntity<PedidoResponseDTO> aceitar(@PathVariable UUID id,
                                                     @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pedidoService.aceitarOrcamento(id, usuario));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id,
                                         @AuthenticationPrincipal Usuario usuario) {
        pedidoService.cancelarPedidoPeloCliente(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
