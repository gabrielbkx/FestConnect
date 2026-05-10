package com.gabriel.party.controllers.midia;

import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.model.midia.enums.TipoMidia;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.midia.MidiaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/midias")
@Tag(name = "Mídias", description = "Endpoints para gerenciamento de mídias de prestadores")
public class MidiaController {

    private final MidiaService midiaService;

    public MidiaController(MidiaService midiaService) {
        this.midiaService = midiaService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mídia criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida, dados incorretos ou faltando"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para enviar mídia"),
            @ApiResponse(responseCode = "404", description = "Prestador ou item não encontrado")
    })
    @Operation(summary = "Enviar mídia", description = "Faz upload de uma imagem ou vídeo para o S3 e associa ao prestador autenticado. " +
            "Opcionalmente vincula a um item do catálogo via itemCatalogoId.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_PRESTADOR')")
    public ResponseEntity<MidiaResponseDTO> criarMidia(@RequestPart("arquivo") MultipartFile arquivo,
                                                       @Valid @RequestPart("dto") MidiaRequestDTO dto,
                                                       @AuthenticationPrincipal Usuario usuario) {
        var midiaCriada = midiaService.salvarMidia(arquivo, dto, usuario.getId());

        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(midiaCriada.id())
                .toUri();
        return ResponseEntity.created(uri).body(midiaCriada);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mídias retornada com sucesso")
    })
    @Operation(summary = "Listar mídias de um prestador", description = "Retorna uma lista paginada de mídias de um prestador, com filtro opcional por tipo.")
    @GetMapping("/{prestadorId}/prestador")
    public ResponseEntity<Page<MidiaResponseDTO>> listarTodasMidiasDoPrestador(
            @PathVariable UUID prestadorId,
            @PageableDefault(size = 10, sort = "ordem") Pageable pageable,
            @RequestParam(required = false) TipoMidia tipoMidia) {
        return ResponseEntity.ok(midiaService.listarMidias(pageable, prestadorId, tipoMidia));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mídia retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    @Operation(summary = "Buscar mídia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<MidiaResponseDTO> buscarMidiaPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(midiaService.buscarMidiaPorId(id));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mídia atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta mídia"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    @Operation(summary = "Atualizar mídia", description = "Atualiza tipo, ordem ou vínculo com item do catálogo.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PRESTADOR', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<MidiaResponseDTO> atualizarMidia(@Valid @RequestBody MidiaRequestDTO dto,
                                                           @PathVariable UUID id,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(midiaService.atualizarMidia(dto, id, usuario.getId()));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mídia deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para deletar esta mídia"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    @Operation(summary = "Deletar mídia", description = "Remove a mídia do S3 e do banco de dados.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_PRESTADOR', 'ROLE_ADMINISTRADOR')")
    public ResponseEntity<Void> deletarMidia(@PathVariable UUID id,
                                             @AuthenticationPrincipal Usuario usuario) {
        midiaService.deletar(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }
}
