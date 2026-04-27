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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO dto) {
        try {
            var tokenAutenticacao = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
            var usuarioAutenticado = authenticationManager.authenticate(tokenAutenticacao);

            if (!(usuarioAutenticado.getPrincipal() instanceof Usuario usuario)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno na sessão.");
            }

            var tokenJwt = tokenService.gerarToken(usuario);
            return ResponseEntity.ok(new TokenResponseDTO(tokenJwt));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E-mail ou senha inválidos.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado.");
        }
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

        // CORREÇÃO: Mudamos de @RequestPart para @RequestBody e removemos a MultipartFile
        var prestadorCadastrado = autenticacaoService.cadastrarPrestador(dto);

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