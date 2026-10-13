package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    private static final String FRONTEND = "http://localhost:5173";
    private static final String EMAIL = "cliente@gmail.com";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenService tokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestadorRepository prestadorRepository;

    @InjectMocks private OAuth2LoginSuccessHandler handler;

    private MockHttpServletRequest request;
    private MockHttpSession sessao;
    private MockHttpServletResponse response;
    private TestingAuthenticationToken autenticacaoGoogle;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "frontendUrl", FRONTEND);

        sessao = new MockHttpSession();
        request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setSession(sessao);
        response = new MockHttpServletResponse();

        // O que o Google devolve depois do login (o "usuário OAuth2")
        var usuarioGoogle = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("OAUTH2_USER")),
                Map.of("sub", "123", "email", EMAIL, "name", "Cliente Google", "picture", "https://foto"),
                "sub");
        autenticacaoGoogle = new TestingAuthenticationToken(usuarioGoogle, null);
    }

    private Usuario usuarioExistente(boolean ativo) {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL);
        usuario.setRole(Role.ROLE_CLIENTE);
        usuario.setAtivo(ativo);
        return usuario;
    }

    @Test
    @DisplayName("o token vai no fragmento (#), nunca na query string (?)")
    void loginOk_tokenNoFragmento() throws Exception {
        var usuario = usuarioExistente(true);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(tokenService.gerarToken(eq(usuario), any(), any(), any())).thenReturn("jwt-gerado");

        handler.onAuthenticationSuccess(request, response, autenticacaoGoogle);

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND + "/oauth2/callback#token=jwt-gerado");
        assertThat(response.getRedirectedUrl()).doesNotContain("?token=");
    }

    @Test
    @DisplayName("a sessão HTTP do fluxo OAuth2 é encerrada depois do login")
    void loginOk_encerraSessao() throws Exception {
        var usuario = usuarioExistente(true);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(tokenService.gerarToken(eq(usuario), any(), any(), any())).thenReturn("jwt-gerado");

        handler.onAuthenticationSuccess(request, response, autenticacaoGoogle);

        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    @DisplayName("conta desativada não recebe token e volta para o login com o motivo")
    void contaDesativada_naoRecebeToken() throws Exception {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuarioExistente(false)));

        handler.onAuthenticationSuccess(request, response, autenticacaoGoogle);

        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND + "/login?erro=conta_desativada");
        assertThat(sessao.isInvalid()).isTrue();
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("primeiro login com Google cria o cliente e também recebe o token no fragmento")
    void primeiroLogin_criaClienteETokenNoFragmento() throws Exception {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(tokenService.gerarToken(any(Usuario.class), isNull(), isNull(), isNull())).thenReturn("jwt-novo");

        handler.onAuthenticationSuccess(request, response, autenticacaoGoogle);

        verify(clienteRepository).save(any(Cliente.class));
        assertThat(response.getRedirectedUrl()).isEqualTo(FRONTEND + "/oauth2/callback#token=jwt-novo");
    }
}
