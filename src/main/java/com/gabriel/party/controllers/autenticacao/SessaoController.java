package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.config.infra.security.TokenDeLogin;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.autenticacao.SessaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fica FORA de /api/v1/auth/** de propósito: aquela rota inteira é permitAll() na SecurityConfig,
 * e encerrar sessões exige um usuário autenticado.
 */
@RestController
@RequestMapping("/api/v1/sessao")
@Tag(name = "Sessão", description = "Encerramento de sessões (logout)")
public class SessaoController {

    private final SessaoService sessaoService;

    public SessaoController(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "A sessão deste dispositivo foi encerrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @Operation(summary = "Sair deste dispositivo",
            description = "Invalida só o token desta requisição. Os outros dispositivos continuam logados.")
    @PostMapping("/logout")
    public ResponseEntity<Void> sairDesteDispositivo(
            // O SecurityFilter guarda nos "details" da autenticação o token que ele já validou
            @Parameter(hidden = true) @CurrentSecurityContext(expression = "authentication.details") TokenDeLogin tokenDeLogin) {
        sessaoService.encerrarEsteDispositivo(tokenDeLogin);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Todas as sessões foram encerradas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @Operation(summary = "Sair de todos os dispositivos",
            description = "Invalida todos os tokens emitidos até agora para o usuário logado, inclusive o desta requisição.")
    @PostMapping("/logout-todos")
    public ResponseEntity<Void> sairDeTodosOsDispositivos(@AuthenticationPrincipal Usuario usuarioLogado) {
        sessaoService.encerrarTodasAsSessoes(usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
