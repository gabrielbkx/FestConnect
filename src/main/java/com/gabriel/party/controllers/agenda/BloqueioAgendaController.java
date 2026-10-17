package com.gabriel.party.controllers.agenda;

import com.gabriel.party.dtos.agenda.BloqueioRequestDTO;
import com.gabriel.party.dtos.agenda.BloqueioResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.agenda.BloqueioAgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agenda/bloqueios")
@Tag(name = "Agenda do prestador", description = "Bloqueios manuais da agenda do próprio prestador")
public class BloqueioAgendaController {

    private final BloqueioAgendaService bloqueioService;

    public BloqueioAgendaController(BloqueioAgendaService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    @Operation(summary = "Criar bloqueio", description = "O prestador marca um período indisponível na própria agenda.")
    @PostMapping
    public ResponseEntity<BloqueioResponseDTO> criar(@Valid @RequestBody BloqueioRequestDTO dto,
                                                     @AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bloqueioService.criar(dto, usuarioLogado));
    }

    @Operation(summary = "Listar meus bloqueios")
    @GetMapping
    public ResponseEntity<List<BloqueioResponseDTO>> listarMeus(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(bloqueioService.listarMeus(usuarioLogado));
    }

    @Operation(summary = "Remover bloqueio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id, @AuthenticationPrincipal Usuario usuarioLogado) {
        bloqueioService.excluir(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
