package com.gabriel.party.controllers.prestador;





import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.services.prestador.PrestadorService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores retornada com sucesso")
    })
    @Operation(summary = "Listar prestadores", description = "Retorna uma lista paginada de todos os prestadores ativos.")
    @GetMapping
    public ResponseEntity<Page<PrestadorResponseDTO>> listarTodosPrestadores(
            @PageableDefault(size = 10, sort = "nome")Pageable pageable) {
        return ResponseEntity.ok(prestadorService.listarPrestadores(pageable));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Prestador retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "Buscar prestador", description = "Busca os detalhes de um prestador ativo pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<PrestadorResponseDTO> buscarPrestadorPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.buscarPrestadorPorId(id));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Prestador atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Prestador ou categoria não encontrado")
    })
    @Operation(summary = "Atualizar prestador", description = "Atualiza os dados de um prestador existente pelo ID.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTADOR','ADMIN')")
    public ResponseEntity<PrestadorResponseDTO> atualizarPrestador(@Valid @RequestBody PrestadorRequestDTO dto, @PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.atualizarPrestador(dto, id));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "204", description = "Prestador inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "Deletar prestador", description = "Realiza a exclusão lógica (inativação) de um prestador pelo ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTADOR','ADMIN')")
    public ResponseEntity<Void> deletarPrestador(@PathVariable UUID id) {
        prestadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores próximos retornada com sucesso")
    })
    @Operation(summary = "Buscar por proximidade", description = "Retorna uma lista de prestadores mais próximos baseada nas coordenadas e raio fornecidos.")
    @GetMapping("/proximidade")
    public ResponseEntity<List<PrestadorResponseDTO>> buscarPorProximidade(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "10.0") Double raio) { // Raio padrão de 10km

        var resultados = prestadorService.buscarPrestadoresProximos(lat, lon, raio);
        return ResponseEntity.ok(resultados);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores filtrada retornada com sucesso")
    })
    @Operation(summary = "Filtrar prestadores por categoria e proximidade",
            description = "Retorna uma lista de prestadores ativos que pertencem a uma categoria específica e estão" +
                    " dentro de um raio definido a partir de coordenadas geográficas.")
    @GetMapping("/filtro-radar")
    public ResponseEntity<List<PrestadorResponseDTO>> filtrarPrestadores(
            @RequestParam UUID categoriaId,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) Double raio) {

        var resultados = prestadorService.buscarPorFiltros(categoriaId, lat, lon, raio);
        return ResponseEntity.ok(resultados);
    }
}
