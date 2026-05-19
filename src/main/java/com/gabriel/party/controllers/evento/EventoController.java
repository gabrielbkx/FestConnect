package com.gabriel.party.controllers.evento;

import com.gabriel.party.dtos.evento.EventoRequestDTO;
import com.gabriel.party.dtos.evento.EventoResponseDTO;
import com.gabriel.party.dtos.evento.EventoStatusUpdateDTO;
import com.gabriel.party.dtos.evento.PrestadorSugestaoDTO;
import com.gabriel.party.dtos.evento.TipoEventoDTO;
import com.gabriel.party.model.evento.enums.StatusEvento;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.evento.EventoService;
import com.gabriel.party.services.evento.EventoTemplateService;
import com.gabriel.party.services.evento.SugestaoService;
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
@RequestMapping("/api/v1/eventos")
@Tag(name = "Eventos", description = "Agrupador opcional de pedidos. Permite ao cliente montar uma festa " +
        "(casamento, 15 anos, churrasco, etc), selecionar categorias de serviço e receber sugestões automáticas " +
        "de prestadores. Cada pedido pode estar vinculado a um evento ou ser avulso.")
public class EventoController {

    private final EventoService eventoService;
    private final EventoTemplateService eventoTemplateService;
    private final SugestaoService sugestaoService;

    public EventoController(EventoService eventoService,
                            EventoTemplateService eventoTemplateService,
                            SugestaoService sugestaoService) {
        this.eventoService = eventoService;
        this.eventoTemplateService = eventoTemplateService;
        this.sugestaoService = sugestaoService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos de evento retornada")
    })
    @Operation(summary = "Tipos de evento disponíveis",
            description = "Retorna todos os tipos de evento que o sistema suporta com suas categorias de serviço sugeridas. Público.")
    @GetMapping("/tipos")
    public ResponseEntity<List<TipoEventoDTO>> listarTipos() {
        return ResponseEntity.ok(eventoTemplateService.listarTipos());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Criar evento",
            description = "Cria um novo evento vinculado ao cliente autenticado. Status inicial: PLANEJANDO.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<EventoResponseDTO> criar(@RequestBody @Valid EventoRequestDTO dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        EventoResponseDTO criado = eventoService.criar(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de eventos do cliente retornada")
    })
    @Operation(summary = "Meus eventos",
            description = "Retorna todos os eventos do cliente autenticado, ordenados por data do evento (mais recentes primeiro). Filtro opcional por status.")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<List<EventoResponseDTO>> listar(@RequestParam(required = false) StatusEvento status,
                                                          @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.listarDoCliente(usuario, status));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhe do evento retornado"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao cliente autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Operation(summary = "Buscar evento por ID",
            description = "Retorna os dados de um evento. Apenas o dono pode acessar.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<EventoResponseDTO> buscarPorId(@PathVariable UUID id,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.buscarPorId(id, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao cliente autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Operation(summary = "Atualizar evento",
            description = "Atualiza os dados de um evento. O status não é alterado por este endpoint — use /status para isso.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<EventoResponseDTO> atualizar(@PathVariable UUID id,
                                                       @RequestBody @Valid EventoRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.atualizar(id, dto, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status do evento atualizado"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao cliente autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Operation(summary = "Atualizar status do evento",
            description = "Transita o evento entre PLANEJANDO, CONFIRMADO e CANCELADO. Transição livre no MVP.")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<EventoResponseDTO> atualizarStatus(@PathVariable UUID id,
                                                             @RequestBody @Valid EventoStatusUpdateDTO dto,
                                                             @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(eventoService.atualizarStatus(id, dto, usuario));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Evento deletado e pedidos desvinculados"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao cliente autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Operation(summary = "Deletar evento",
            description = "Remove o evento. Pedidos vinculados continuam existindo, apenas perdem o agrupamento (evento_id = null).")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id,
                                        @AuthenticationPrincipal Usuario usuario) {
        eventoService.deletar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores sugeridos retornada"),
            @ApiResponse(responseCode = "403", description = "Evento não pertence ao cliente autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento ou categoria não encontrado")
    })
    @Operation(summary = "Sugestões de prestadores para o evento",
            description = "Retorna prestadores sugeridos para a categoria informada, filtrados pela cidade do evento " +
                    "e ordenados por avaliação com leve aleatoriedade dentro do top. Default: 3 prestadores. Máximo: 20.")
    @GetMapping("/{id}/sugestoes")
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<List<PrestadorSugestaoDTO>> sugestoes(@PathVariable UUID id,
                                                                @RequestParam String categoria,
                                                                @RequestParam(required = false, defaultValue = "3") Integer limite,
                                                                @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(sugestaoService.sugerirPrestadores(id, categoria, limite, usuario));
    }
}
