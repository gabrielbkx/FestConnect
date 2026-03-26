package com.gabriel.party.controllers.categoria;


import com.gabriel.party.dtos.categoria.CategoriaReponseDTO;
import com.gabriel.party.dtos.categoria.CategoriaRequestDTO;
import com.gabriel.party.services.categoria.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
public class CategoriaController {

    CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria e a retorna.")
    @PostMapping
    public ResponseEntity<CategoriaReponseDTO> criarCategoria(@Valid @RequestBody CategoriaRequestDTO dto){

        var categoriaCriada = categoriaService.salvarCategoria(dto);

        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoriaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(categoriaCriada);
    }

    @Operation(summary = "Listar todas as categorias", description = "Retorna uma lista paginada de categorias ativas.")
    @GetMapping
    public ResponseEntity<Page<CategoriaReponseDTO>> listarTodasCategorias(
            @PageableDefault(size = 10, sort = "nome")Pageable pageable){

        return ResponseEntity.ok(categoriaService.listarCategorias(pageable));
    }

    @Operation(summary = "Deletar categoria", description = "Realiza a exclusão lógica (inativação) de uma categoria pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCategoria(@PathVariable UUID id){
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria existente pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaReponseDTO> atualizarCategoria(@Valid @RequestBody CategoriaRequestDTO dto,
                                                                  @PathVariable UUID id){
        var categoriaAtualizada = categoriaService.atualizarCategoria(dto, id);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    @Operation(summary = "Buscar categoria", description = "Busca os detalhes de uma categoria ativa pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaReponseDTO> buscarCategoriaPorId(@PathVariable UUID id){
        var categoria = categoriaService.buscarCategoriaPorId(id);
        return ResponseEntity.ok(categoria);
    }
}
