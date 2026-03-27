package com.gabriel.party.controllers.prestador;


import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.services.prestador.PrestadorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/prestadores")
@Tag(name = "Prestadores", description = "Endpoints para gerenciamento de prestadores")
public class PrestadorController {

    private final PrestadorService prestadorService;

    public PrestadorController(PrestadorService prestadorService) {
        this.prestadorService = prestadorService;
    }

    @Operation(summary = "Criar novo prestador", description = "Cria um novo prestador associado a uma categoria e o retorna.")
    @PostMapping
    public ResponseEntity<PrestadorResponseDTO> criarPrestador(@Valid @RequestBody PrestadorRequestDTO dto) {
        var prestadorCriado = prestadorService.salvarPrestador(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(prestadorCriado.id())
                .toUri();
        return ResponseEntity.created(uri).body(prestadorCriado);
    }

    @Operation(summary = "Listar prestadores", description = "Retorna uma lista paginada de todos os prestadores ativos.")
    @GetMapping
    public ResponseEntity<Page<PrestadorResponseDTO>> listarTodosPrestadores(
            @PageableDefault(size = 10, sort = "nome")Pageable pageable) {
        return ResponseEntity.ok(prestadorService.listarPrestadores(pageable));
    }

    @Operation(summary = "Buscar prestador", description = "Busca os detalhes de um prestador ativo pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<PrestadorResponseDTO> buscarPrestadorPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.buscarPrestadorPorId(id));
    }

    @Operation(summary = "Atualizar prestador", description = "Atualiza os dados de um prestador existente pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<PrestadorResponseDTO> atualizarPrestador(@Valid @RequestBody PrestadorRequestDTO dto, @PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.atualizarPrestador(dto, id));
    }

    @Operation(summary = "Deletar prestador", description = "Realiza a exclusão lógica (inativação) de um prestador pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPrestador(@PathVariable UUID id) {
        prestadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/proximidade")
    public ResponseEntity<List<PrestadorResponseDTO>> buscarPorProximidade(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "10.0") Double raio) { // Raio padrão de 10km

        var resultados = prestadorService.buscarPrestadoresProximos(lat, lon, raio);
        return ResponseEntity.ok(resultados);
    }
}
