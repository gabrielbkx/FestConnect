package com.gabriel.party.controllers.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.services.itemcatalogo.ItemCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    @Operation(summary = "Criar novo item de catálogo", description = "Cria um novo item (produto ou serviço) para um prestador.")
    @PostMapping
    public ResponseEntity<ItemCatalogoResponseDTO> criarItem(@Valid @RequestBody ItemCatalogoRequestDTO dto){

        var itemCriado = itemCatalogoService.criarItem(dto);

        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(itemCriado.id())
                .toUri();

        return ResponseEntity.created(uri).body(itemCriado);
    }

    @Operation(summary = "Listar todos os itens", description = "Retorna uma lista paginada de todos os itens ativos.")
    @GetMapping
    public ResponseEntity<Page<ItemCatalogoResponseDTO>> listarTodosItens(
            @PageableDefault(size = 10, sort = "titulo") Pageable pageable){
        return ResponseEntity.ok(itemCatalogoService.listarItensCatalogo(pageable));
    }

    @Operation(summary = "Listar vitrine do prestador", description = "Retorna todos os itens ativos associados a um prestador específico.")
    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<List<ItemCatalogoResponseDTO>> listarVitrineDoPrestador(@PathVariable UUID prestadorId){
        return ResponseEntity.ok(itemCatalogoService.listarVitrineDoPrestador(prestadorId));
    }

    @Operation(summary = "Deletar item de catálogo", description = "Realiza a exclusão lógica (inativação) de um item pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarItemCatalogo(@PathVariable UUID id){
        itemCatalogoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar item de catálogo", description = "Atualiza os dados de um item de catálogo existente pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ItemCatalogoResponseDTO> atualizarItemCatalogo(@Valid @RequestBody ItemCatalogoRequestDTO dto,
                                                                         @PathVariable UUID id){
        var itemAtualizado = itemCatalogoService.atualizarItem(dto, id);
        return ResponseEntity.ok(itemAtualizado);
    }

    @Operation(summary = "Buscar item", description = "Busca os detalhes de um item ativo pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ItemCatalogoResponseDTO> buscarItemPorId(@PathVariable UUID id){
        var item = itemCatalogoService.buscarItemPorId(id);
        return ResponseEntity.ok(item);
    }
}

