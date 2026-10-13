package com.gabriel.party.config.infra.security;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    private static final String TOKEN = "token-qualquer";
    private static final String EMAIL = "cliente@festconnect.com";

    @Mock private TokenService tokenService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private SecurityFilter filtro;

    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void limparContexto() {
        // O SecurityContextHolder é estático (por thread): sem limpar, um teste vaza para o outro
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequest requisicaoComToken() {
        var request = new MockHttpServletRequest("GET", "/api/v1/categorias");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }

    private Usuario usuario(boolean ativo, boolean emailVerificado) {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL);
        usuario.setRole(Role.ROLE_CLIENTE);
        usuario.setAtivo(ativo);
        usuario.setEmailVerificado(emailVerificado);
        return usuario;
    }

    @Test
    @DisplayName("sem token: segue anônimo, sem consultar nada")
    void semToken_segueAnonimo() throws Exception {
        filtro.doFilter(new MockHttpServletRequest("GET", "/api/v1/categorias"), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService, usuarioRepository);
    }

    @Test
    @DisplayName("token vencido/inválido: NÃO lança exceção e a requisição segue anônima")
    void tokenInvalido_segueAnonimoSemExcecao() throws Exception {
        when(tokenService.validarTokenDeLogin(TOKEN))
                .thenThrow(new AppException(ErrorCode.ERRO_AO_VALIDAR_TOKEN_JWT, "Token JWT inválido ou expirado"));

        filtro.doFilter(requisicaoComToken(), response, chain);

        // A cadeia continuou: quem decide 200 (rota pública) ou 401 (protegida) é a SecurityConfig
        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("token de usuário que não existe mais: segue anônimo (antes: NoSuchElementException)")
    void usuarioInexistente_segueAnonimo() throws Exception {
        when(tokenService.validarTokenDeLogin(TOKEN)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        filtro.doFilter(requisicaoComToken(), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("usuário desativado perde o acesso na próxima requisição, mesmo com token dentro da validade")
    void usuarioDesativado_segueAnonimo() throws Exception {
        when(tokenService.validarTokenDeLogin(TOKEN)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(false, true)));

        filtro.doFilter(requisicaoComToken(), response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("usuário ativo é autenticado com os papéis dele")
    void usuarioAtivo_autentica() throws Exception {
        var usuario = usuario(true, true);
        when(tokenService.validarTokenDeLogin(TOKEN)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        filtro.doFilter(requisicaoComToken(), response, chain);

        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacao).isNotNull();
        assertThat(autenticacao.getPrincipal()).isSameAs(usuario);
        assertThat(autenticacao.getAuthorities()).extracting("authority").containsExactly("ROLE_CLIENTE");
    }

    @Test
    @DisplayName("e-mail ainda não verificado continua autenticando (decisão: o token do cadastro precisa funcionar)")
    void emailNaoVerificado_aindaAutentica() throws Exception {
        when(tokenService.validarTokenDeLogin(TOKEN)).thenReturn(EMAIL);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario(true, false)));

        filtro.doFilter(requisicaoComToken(), response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }
}
