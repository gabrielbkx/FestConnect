package com.gabriel.party.controllers.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.itemcatalogo.ItemCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/itens-catalogo")
@Tag(name = "Itens do Catálogo", description = "Endpoints para gerenciamento do catálogo de itens (produtos/serviços) dos prestadores")
public class ItemCatalogoController {

    private final ItemCatalogoService itemCatalogoService;

    public ItemCatalogoController(ItemCatalogoService itemCatalogoService) {
        this.itemCatalogoService = itemCatalogoService;
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida, dados incorretos ou faltando"),
            @ApiResponse(responseCode = "404", description = "Prestador associado não encontrado")
    })
    @Operation(summary = "Criar novo item de catálogo", description = "Cria um novo item (produto ou serviço) para um prestador.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<ItemCatalogoResponseDTO> criarItem(@Valid @RequestBody ItemCatalogoRequestDTO dto,
                                                             @AuthenticationPrincipal Usuario usuario){

        UUID usuarioLogado = usuario.getId();

        var itemCriado = itemCatalogoService.criarItem(dto, usuarioLogado);

        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(itemCriado.id())
                .toUri();

        return ResponseEntity.created(uri).body(itemCriado);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de itens retornada com sucesso")
    })
    @Operation(summary = "Listar todos os itens", description = "Retorna uma lista paginada de todos os itens ativos.")
    @GetMapping
    public ResponseEntity<Page<ItemCatalogoResponseDTO>> listarTodosItens(
            @PageableDefault(size = 10, sort = "titulo") Pageable pageable){
        return ResponseEntity.ok(itemCatalogoService.listarItensCatalogo(pageable));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Vitrine do prestador retornada com sucesso")
    })
    @Operation(summary = "Listar vitrine do prestador", description = "Retorna todos os itens ativos associados a um prestador específico.")
    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<ItemCatalogoResponseDTO>> listarVitrineDoPrestador(@PathVariable UUID prestadorId){
        return ResponseEntity.ok(itemCatalogoService.listarVitrineDoPrestador(prestadorId));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "204", description = "Item inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de catálogo não encontrado")
    })
    @Operation(summary = "Deletar item de catálogo", description = "Realiza a exclusão lógica (inativação) de um item pelo ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<Void> deletarItemCatalogo(@PathVariable UUID id){
        itemCatalogoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Item de catálogo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Item de catálogo não encontrado")
    })
    @Operation(summary = "Atualizar item de catálogo", description = "Atualiza os dados de um item de catálogo existente pelo ID.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<ItemCatalogoResponseDTO> atualizarItemCatalogo(@Valid @RequestBody ItemCatalogoRequestDTO dto,
                                                                         @PathVariable UUID idItem,
                                                                         @AuthenticationPrincipal Usuario usuario){
        UUID idUsuarioLogado = usuario.getId();
        var itemAtualizado = itemCatalogoService.atualizarItem(dto, idItem, idUsuarioLogado);
        return ResponseEntity.ok(itemAtualizado);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Item de catálogo retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item de catálogo não encontrado")
    })
    @Operation(summary = "Buscar item", description = "Busca os detalhes de um item ativo pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ItemCatalogoResponseDTO> buscarItemPorId(@PathVariable UUID id){
        var item = itemCatalogoService.buscarItemPorId(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/filtro-radar")
    public ResponseEntity<Page<ItemCatalogoResponseDTO>> buscarItensNoRadar(
            @RequestParam(name = "busca", required = false) String busca,
            @RequestParam(name = "lat") Double lat,
            @RequestParam(name = "lon") Double lon,
            @RequestParam(name = "raio", defaultValue = "5.0") Double raio,
            @PageableDefault(size = 10, page = 0) Pageable pageable // O Spring faz a mágica aqui
    ) {
        // Agora você só repassa o objeto pageable direto pro Service
        Page<ItemCatalogoResponseDTO> itensEncontrados = itemCatalogoService.buscarItensPorRadarEBusca(busca, lat, lon, raio, pageable);

        return ResponseEntity.ok(itensEncontrados);
    }
}
