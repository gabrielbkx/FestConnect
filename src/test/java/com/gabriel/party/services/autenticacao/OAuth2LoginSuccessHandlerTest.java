package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Nested
    @DisplayName("onAuthenticationSuccess")
    class OnAuthenticationSuccess {

        @Test
        @DisplayName("Deve autenticar usuário existente via OAuth2")
        void deveAutenticarUsuarioExistente() throws Exception {
            var usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setEmail("existente@gmail.com");
            usuario.setRole(Role.ROLE_CLIENTE);

            when(authentication.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2User.getAttribute("email")).thenReturn("existente@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("João Existente");
            when(oAuth2User.getAttribute("picture")).thenReturn("https://foto.jpg");
            when(usuarioRepository.findByEmail("existente@gmail.com")).thenReturn(Optional.of(usuario));
            when(tokenService.gerarToken(usuario)).thenReturn("jwt-token-existente");
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).setContentType("application/json");
            verify(response).setCharacterEncoding("UTF-8");
            verify(clienteRepository, never()).save(any(Cliente.class));

            String jsonOutput = stringWriter.toString();
            assertThat(jsonOutput).contains("jwt-token-existente");
        }

        @Test
        @DisplayName("Deve criar novo usuário via OAuth2 quando não existe")
        void deveCriarNovoUsuarioViaOAuth2() throws Exception {
            when(authentication.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2User.getAttribute("email")).thenReturn("novo@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Novo Usuário");
            when(oAuth2User.getAttribute("picture")).thenReturn("https://foto-nova.jpg");
            when(usuarioRepository.findByEmail("novo@gmail.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("senhaEncriptada");
            when(tokenService.gerarToken(any(Usuario.class))).thenReturn("jwt-token-novo");
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationSuccess(request, response, authentication);

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());

            Cliente clienteSalvo = clienteCaptor.getValue();
            assertThat(clienteSalvo.getNomeCompleto()).isEqualTo("Novo Usuário");
            assertThat(clienteSalvo.getFotoPerfilUrl()).isEqualTo("https://foto-nova.jpg");
            assertThat(clienteSalvo.getUsuario()).isNotNull();
            assertThat(clienteSalvo.getUsuario().getEmail()).isEqualTo("novo@gmail.com");
            assertThat(clienteSalvo.getUsuario().getRole()).isEqualTo(Role.ROLE_CLIENTE);

            String jsonOutput = stringWriter.toString();
            assertThat(jsonOutput).contains("jwt-token-novo");
        }

        @Test
        @DisplayName("Deve retornar resposta JSON com token")
        void deveRetornarRespostaJsonComToken() throws Exception {
            var usuario = new Usuario();
            usuario.setId(UUID.randomUUID());
            usuario.setEmail("teste@gmail.com");
            usuario.setRole(Role.ROLE_CLIENTE);

            when(authentication.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2User.getAttribute("email")).thenReturn("teste@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Teste");
            when(oAuth2User.getAttribute("picture")).thenReturn("https://foto.jpg");
            when(usuarioRepository.findByEmail("teste@gmail.com")).thenReturn(Optional.of(usuario));
            when(tokenService.gerarToken(usuario)).thenReturn("meu-jwt-token");
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).setContentType("application/json");
            verify(response).setCharacterEncoding("UTF-8");

            String jsonOutput = stringWriter.toString();
            assertThat(jsonOutput).contains("\"token\"");
            assertThat(jsonOutput).contains("meu-jwt-token");
        }

        @Test
        @DisplayName("Deve definir role ROLE_CLIENTE para novo usuário OAuth2")
        void deveDefinirRoleClienteParaNovoUsuario() throws Exception {
            when(authentication.getPrincipal()).thenReturn(oAuth2User);
            when(oAuth2User.getAttribute("email")).thenReturn("novo@gmail.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Novo");
            when(oAuth2User.getAttribute("picture")).thenReturn(null);
            when(usuarioRepository.findByEmail("novo@gmail.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("senhaEncriptada");
            when(tokenService.gerarToken(any(Usuario.class))).thenReturn("token");
            when(response.getWriter()).thenReturn(printWriter);

            handler.onAuthenticationSuccess(request, response, authentication);

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());

            Usuario usuarioCriado = clienteCaptor.getValue().getUsuario();
            assertThat(usuarioCriado.getRole()).isEqualTo(Role.ROLE_CLIENTE);
        }
    }
}
