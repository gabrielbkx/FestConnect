package com.gabriel.party.controllers.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.services.avaliacao.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Criar nova avaliação",
            description = "Cria uma nova avaliação e a associa a um prestador.")
    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> criarAvaliacao(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        var avaliacaoCriada = avaliacaoService.salvarAvaliacao(dto);
        var uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(avaliacaoCriada.id())
                .toUri();
        return ResponseEntity.created(uri).body(avaliacaoCriada);
    }

    @Operation(summary = "Listar todas as avaliações",
            description = "Retorna uma lista paginada de todas as avaliações ativas.")
    @GetMapping
    public ResponseEntity<Page<AvaliacaoResponseDTO>> listarTodasAvaliacoes(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarAvaliacoes(pageable));
    }

    @Operation(summary = "Buscar avaliação",
            description = "Busca os detalhes de uma avaliação ativa pelo ID.")
    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> buscarAvaliacaoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(avaliacaoService.buscarAvaliacaoPorId(id));
    }

    @Operation(summary = "Atualizar avaliação",
            description = "Atualiza os dados de uma avaliação existente pelo ID.")
    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponseDTO> atualizarAvaliacao(@Valid @RequestBody AvaliacaoRequestDTO dto, @PathVariable UUID id) {
        return ResponseEntity.ok(avaliacaoService.atualizarAvaliacao(dto, id));
    }

    @Operation(summary = "Deletar avaliação",
            description = "Realiza a exclusão lógica (inativação) de uma avaliação pelo ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAvaliacao(@PathVariable UUID id) {
        avaliacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
