package com.gabriel.party.controllers.admin;

import com.gabriel.party.services.itemcatalogo.ItemCatalogoService;
import com.gabriel.party.services.midia.MidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
@Tag(name = "Moderação (Admin)", description = "Endpoints de moderação de conteúdo, restritos ao administrador")
public class AdminModeracaoController {

    private final MidiaService midiaService;
    private final ItemCatalogoService itemCatalogoService;

    public AdminModeracaoController(MidiaService midiaService, ItemCatalogoService itemCatalogoService) {
        this.midiaService = midiaService;
        this.itemCatalogoService = itemCatalogoService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Mídia removida com sucesso"),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem moderar conteúdo"),
            @ApiResponse(responseCode = "404", description = "Mídia não encontrada")
    })
    @Operation(summary = "Moderar (remover) mídia de qualquer prestador",
            description = "Remove a mídia do S3 e do banco, independentemente do dono, e notifica o prestador por e-mail.")
    @DeleteMapping("/midias/{id}")
    public ResponseEntity<Void> moderarMidia(@PathVariable UUID id) {
        midiaService.deletarComoModerador(id);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Apenas administradores podem moderar conteúdo"),
            @ApiResponse(responseCode = "404", description = "Item de catálogo não encontrado")
    })
    @Operation(summary = "Moderar (remover) item de catálogo de qualquer prestador",
            description = "Inativa (soft-delete) o item, independentemente do dono, e notifica o prestador por e-mail.")
    @DeleteMapping("/itens-catalogo/{id}")
    public ResponseEntity<Void> moderarItemCatalogo(@PathVariable UUID id) {
        itemCatalogoService.removerComoModerador(id);
        return ResponseEntity.noContent().build();
    }
}