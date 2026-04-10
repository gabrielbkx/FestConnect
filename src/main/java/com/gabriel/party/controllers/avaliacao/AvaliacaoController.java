package com.gabriel.party.controllers.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoCreateDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.avaliacao.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/avaliacoes")
@Tag(name = "Avaliações", description = "Endpoints para gerenciamento de avaliações de prestadores")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "Avaliação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida, dados incorretos ou faltando"),
            @ApiResponse(responseCode = "404", description = "Prestador associado não encontrado")
    })
    @Operation(summary = "Criar nova avaliação",
            description = "Cria uma nova avaliação e a associa a um prestador.")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLIENTE')")
    public ResponseEntity<AvaliacaoResponseDTO> criarAvaliacao(@Valid @RequestBody AvaliacaoCreateDTO dto,
                                                               @AuthenticationPrincipal Usuario usuario) {

        var usuarioLogado = usuario.getId();

        var avaliacaoCriada = avaliacaoService.salvarAvaliacao(dto, usuarioLogado);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(avaliacaoCriada.id())
                .toUri();
        return ResponseEntity.created(uri).body(avaliacaoCriada);
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso")
    })
    @Operation(summary = "Listar todas as avaliações",
            description = "Retorna uma lista paginada de todas as avaliações ativas.")
    @GetMapping
    public ResponseEntity<Page<AvaliacaoResponseDTO>> listarTodasAvaliacoes(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoes(pageable));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Avaliação retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @Operation(summary = "Buscar avaliação",
            description = "Busca os detalhes de uma avaliação ativa pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> buscarAvaliacaoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacaoPorId(id));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @Operation(summary = "Atualizar avaliação",
            description = "Atualiza os dados de uma avaliação existente pelo ID.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<AvaliacaoResponseDTO> atualizarAvaliacao(@Valid @RequestBody AvaliacaoRequestDTO dto,
                                                                   @PathVariable UUID idAvaliacao,
                                                                   @AuthenticationPrincipal Usuario usuario) {

        var usuarioLogado = usuario.getId();

        return ResponseEntity.ok(avaliacaoService.atualizarAvaliacao(dto, idAvaliacao, usuarioLogado));
    }

    @ApiResponses( value = {
            @ApiResponse(responseCode = "204", description = "Avaliação inativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada")
    })
    @Operation(summary = "Deletar avaliação",
            description = "Realiza a exclusão lógica (inativação) de uma avaliação pelo ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRADOR', 'ROLE_CLIENTE')")
    public ResponseEntity<Void> deletarAvaliacao(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuario) {

        avaliacaoService.deletar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
