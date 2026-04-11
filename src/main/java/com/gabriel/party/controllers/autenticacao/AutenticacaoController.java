package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.dtos.autenticacao.DadosDeValidacaoDeCodigoRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRedefinicaoDeSenhaDTO;
import com.gabriel.party.services.autenticacao.AutenticacaoService;
import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.autenticacao.login.LoginRequestDTO;
import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AutenticacaoService autenticacaoService;


    public AutenticacaoController(TokenService tokenService,
                                  AuthenticationManager authenticationManager,
                                  AutenticacaoService autenticacaoService, ArmazenamentoService armazenamentoService) {
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
    public ResponseEntity<CadastroResponseDTO> cadastrarCliente(@RequestPart("dados") @Valid CadastroClienteDTO dto,
                                                                @RequestPart(value = "foto", required = false)
                                                                MultipartFile fotoPerfil) {

        var clienteCadastrado = autenticacaoService.cadastrarCliente(dto, fotoPerfil);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(clienteCadastrado.id()).toUri();

        return ResponseEntity.created(uri).body(clienteCadastrado);
    }

    @PostMapping("/cadastro/prestador")
    public ResponseEntity<CadastroResponseDTO> cadastrarPrestador(@RequestPart("dados") @Valid CadastroPrestadorDTO dto,
                                                                  @RequestPart(value = "foto", required = false)
                                                                  MultipartFile fotoPerfil) {

        var prestadorCadastrado = autenticacaoService.cadastrarPrestador(dto, fotoPerfil);

        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(prestadorCadastrado.id()).toUri();

        return ResponseEntity.created(uri).body(prestadorCadastrado);
    }

    @PostMapping("/recuperacao-senha")
    public ResponseEntity<Void> iniciarRecuperacaoSenha(@RequestBody DadosRecuperacaoDTO email) {
        autenticacaoService.enviarCodigoRecuperacao(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<TokenResponseDTO> validarCodigoRecuperacao(@RequestBody DadosDeValidacaoDeCodigoRecuperacaoDTO dados) {

        var tokenRecuperacao = autenticacaoService.validarCodigoRecuperacao(dados);
        return ResponseEntity.ok().body(new TokenResponseDTO(tokenRecuperacao));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody DadosRedefinicaoDeSenhaDTO dados) {
        autenticacaoService.redefinirSenha(dados);
        return ResponseEntity.ok().build();
    }
}
