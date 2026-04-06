package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.config.infra.security.AutenticacaoService;
import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroRequestDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.autenticacao.login.LoginRequestDTO;
import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(TokenService tokenService,
                                  AuthenticationManager authenticationManager,
                                  AutenticacaoService autenticacaoService) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        var tokenAutenticacao = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());

        var usuarioAutenticado = authenticationManager.authenticate(tokenAutenticacao);

        var usuario = (Usuario) usuarioAutenticado.getPrincipal();
        var tokenJwt = tokenService.gerarToken(usuario);

        return ResponseEntity.ok(new TokenResponseDTO(tokenJwt));
    }

    @PostMapping("/cadastro/cliente")
    public ResponseEntity<CadastroResponseDTO> cadastrarCliente(@RequestBody @Valid CadastroClienteDTO dto) {
        var clienteCadastrado = autenticacaoService.cadastrarCliente(dto);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(clienteCadastrado.id()).toUri();

        return ResponseEntity.created(uri).body(clienteCadastrado);
    }

    @PostMapping("/cadastro/prestador")
    public ResponseEntity<CadastroResponseDTO> cadastrarPrestador(@RequestBody @Valid CadastroPrestadorDTO dto) {
        var prestadorCadastrado = autenticacaoService.cadastrarPrestador(dto);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(prestadorCadastrado.id()).toUri();

        return ResponseEntity.created(uri).body(prestadorCadastrado);
    }
}
