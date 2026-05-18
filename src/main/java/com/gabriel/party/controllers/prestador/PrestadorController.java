package com.gabriel.party.controllers.prestador;





import com.gabriel.party.dtos.prestador.PrestadorAdminDTO;
import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.dtos.prestador.PrestadorResumoDTO;
import com.gabriel.party.services.prestador.PrestadorService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/prestadores")
@Tag(name = "Prestadores", description = "Endpoints para gerenciamento de prestadores")
public class PrestadorController {

    private final PrestadorService prestadorService;

    public PrestadorController(PrestadorService prestadorService) {
        this.prestadorService = prestadorService;
    }


    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores retornada com sucesso")
    })
    @Operation(summary = "Listar prestadores", description = "Retorna uma lista paginada de prestadores ativos, com filtros opcionais por categoria e busca por nome.")
    @GetMapping
    public ResponseEntity<Page<PrestadorResumoDTO>> listarTodosPrestadores(
            @RequestParam(required = false) List<UUID> categoriaId,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String cidade,
            @PageableDefault(size = 10, sort = "nomeCompleto") Pageable pageable) {
        return ResponseEntity.ok(prestadorService.listarPrestadores(categoriaId, busca, cidade, pageable));
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

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhe administrativo do prestador retornado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — somente administradores"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "[ADMIN] Detalhe completo do prestador",
            description = "Retorna o perfil completo do prestador com dados sensíveis (CNPJ/CPF, " +
                    "endereço completo, WhatsApp, data de criação). Acesso restrito a ROLE_ADMINISTRADOR. " +
                    "Inclui prestadores inativos.")
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<PrestadorAdminDTO> buscarDetalheAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.buscarDetalheAdmin(id));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Prestador atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Prestador ou categoria não encontrado")
    })
    @Operation(summary = "Atualizar prestador", description = "Atualiza os dados de um prestador existente pelo ID.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PRESTADOR', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<PrestadorResponseDTO> atualizarPrestador(@Valid @RequestBody PrestadorRequestDTO dto, @PathVariable UUID id) {
        return ResponseEntity.ok(prestadorService.atualizarPrestador(dto, id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto de perfil atualizada"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato não suportado"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "Atualizar foto de perfil", description = "Faz upload de uma nova foto de perfil. A foto anterior é removida do storage.")
    @PutMapping(value = "/{id}/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_PRESTADOR', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Map<String, String>> atualizarFotoPerfil(@PathVariable UUID id,
                                                                    @RequestPart("arquivo") MultipartFile arquivo) {
        String url = prestadorService.atualizarFotoPerfil(id, arquivo);
        return ResponseEntity.ok(Map.of("fotoPerfilUrl", url));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "204", description = "Prestador inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Prestador não encontrado")
    })
    @Operation(summary = "Deletar prestador", description = "Realiza a exclusão lógica (inativação) de um prestador pelo ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PRESTADOR', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> deletarPrestador(@PathVariable UUID id) {
        prestadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de prestadores próximos retornada com sucesso")
    })
    @Operation(summary = "Buscar por proximidade", description = "Retorna uma lista de prestadores mais próximos baseada nas coordenadas e raio fornecidos.")
    @GetMapping("/proximidade")
    public ResponseEntity<List<PrestadorResumoDTO>> buscarPorProximidade(
            @Parameter(description = "Latitude da localização do cliente", example = "-23.5505") @RequestParam Double lat,
            @Parameter(description = "Longitude da localização do cliente", example = "-46.6333") @RequestParam Double lon,
            @Parameter(description = "Raio de busca em quilômetros", example = "10.0") @RequestParam(defaultValue = "10.0") Double raio) {

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
    public ResponseEntity<List<PrestadorResumoDTO>> filtrarPrestadores(
            @Parameter(description = "ID da categoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @RequestParam UUID categoriaId,
            @Parameter(description = "Latitude da localização do cliente", example = "-23.5505") @RequestParam Double lat,
            @Parameter(description = "Longitude da localização do cliente", example = "-46.6333") @RequestParam Double lon,
            @Parameter(description = "Raio de busca em quilômetros. Padrão: 50 km", example = "20.0") @RequestParam(required = false) Double raio) {

        var resultados = prestadorService.buscarPorFiltros(categoriaId, lat, lon, raio);
        return ResponseEntity.ok(resultados);
    }
}
