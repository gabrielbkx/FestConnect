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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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

    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida, dados incorretos ou faltando"),
            @ApiResponse(responseCode = "409", description = "Conflito, categoria já existe")
    })
    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria e a retorna.")
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaReponseDTO> criarCategoria(@Valid @RequestBody CategoriaRequestDTO dto){

        var categoriaCriada = categoriaService.salvarCategoria(dto);

        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoriaCriada.id())
                .toUri();

        return ResponseEntity.created(uri).body(categoriaCriada);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso")
    })
    @Operation(summary = "Listar todas as categorias", description = "Retorna uma lista paginada de categorias ativas.")
    @GetMapping
    public ResponseEntity<Page<CategoriaReponseDTO>> listarTodasCategorias(
            @PageableDefault(size = 10, sort = "nome")Pageable pageable){

        return ResponseEntity.ok(categoriaService.listarCategorias(pageable));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "204", description = "Categoria inativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @Operation(summary = "Deletar categoria", description = "Realiza a exclusão lógica (inativação) de uma categoria pelo ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarCategoria(@PathVariable UUID id){
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria existente pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaReponseDTO> atualizarCategoria(@Valid @RequestBody CategoriaRequestDTO dto,
                                                                  @PathVariable UUID id){
        var categoriaAtualizada = categoriaService.atualizarCategoria(dto, id);
        return ResponseEntity.ok(categoriaAtualizada);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Categoria retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @Operation(summary = "Buscar categoria", description = "Busca os detalhes de uma categoria ativa pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaReponseDTO> buscarCategoriaPorId(@PathVariable UUID id){
        var categoria = categoriaService.buscarCategoriaPorId(id);
        return ResponseEntity.ok(categoria);
    }
}
