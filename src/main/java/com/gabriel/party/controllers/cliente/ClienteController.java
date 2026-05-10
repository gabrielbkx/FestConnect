package com.gabriel.party.controllers.cliente;

import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.dtos.cliente.ClienteResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorResumoDTO;
import com.gabriel.party.services.cliente.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de perfis de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Buscar cliente por ID", description = "Retorna os dados de um cliente ativo pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes retornada com sucesso")
    })
    @Operation(summary = "Listar clientes", description = "Retorna uma lista paginada de todos os clientes ativos.")
    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarTodos(Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarTodos(pageable));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar este cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados do perfil de um cliente. Requer autenticação como CLIENTE ou ADMIN.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable UUID id,
                                                        @RequestBody @Valid ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente inativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este cliente"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Excluir cliente", description = "Realiza a exclusão lógica (inativação) do perfil de um cliente.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // ── Favoritos: Prestadores

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Prestador adicionado aos favoritos"),
            @ApiResponse(responseCode = "404", description = "Cliente ou prestador não encontrado"),
            @ApiResponse(responseCode = "409", description = "Prestador já está nos favoritos")
    })
    @Operation(summary = "Adicionar prestador aos favoritos")
    @PostMapping("/{clienteId}/favoritos/prestadores/{prestadorId}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> adicionarPrestadorFavorito(@PathVariable UUID clienteId,
                                                            @PathVariable UUID prestadorId) {
        clienteService.adicionarPrestadorFavorito(clienteId, prestadorId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Prestador removido dos favoritos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou prestador não está nos favoritos")
    })
    @Operation(summary = "Remover prestador dos favoritos")
    @DeleteMapping("/{clienteId}/favoritos/prestadores/{prestadorId}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> removerPrestadorFavorito(@PathVariable UUID clienteId,
                                                          @PathVariable UUID prestadorId) {
        clienteService.removerPrestadorFavorito(clienteId, prestadorId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores favoritos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Listar prestadores favoritos do cliente")
    @GetMapping("/{clienteId}/favoritos/prestadores")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<PrestadorResumoDTO>> listarPrestadoresFavoritos(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(clienteService.listarPrestadoresFavoritos(clienteId));
    }

    // ── Favoritos: Categorias

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria adicionada aos favoritos"),
            @ApiResponse(responseCode = "404", description = "Cliente ou categoria não encontrada"),
            @ApiResponse(responseCode = "409", description = "Categoria já está nos favoritos")
    })
    @Operation(summary = "Adicionar categoria aos favoritos")
    @PostMapping("/{clienteId}/favoritos/categorias/{categoriaId}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> adicionarCategoriaFavorita(@PathVariable UUID clienteId,
                                                            @PathVariable UUID categoriaId) {
        clienteService.adicionarCategoriaFavorita(clienteId, categoriaId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria removida dos favoritos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou categoria não está nos favoritos")
    })
    @Operation(summary = "Remover categoria dos favoritos")
    @DeleteMapping("/{clienteId}/favoritos/categorias/{categoriaId}")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> removerCategoriaFavorita(@PathVariable UUID clienteId,
                                                          @PathVariable UUID categoriaId) {
        clienteService.removerCategoriaFavorita(clienteId, categoriaId);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias favoritas retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @Operation(summary = "Listar categorias favoritas do cliente")
    @GetMapping("/{clienteId}/favoritos/categorias")
    @PreAuthorize("hasAnyRole('ROLE_CLIENTE', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<CategoriaReponseDTO>> listarCategoriasFavoritas(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(clienteService.listarCategoriasFavoritas(clienteId));
    }
}
