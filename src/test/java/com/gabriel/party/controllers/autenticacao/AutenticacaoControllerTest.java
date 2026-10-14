package com.gabriel.party.controllers.autenticacao;

import com.gabriel.party.config.exception.GlobalExceptionHandler;
import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.exceptions.MuitasTentativasException;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.autenticacao.AutenticacaoService;
import com.gabriel.party.services.autenticacao.LimiteDeLogin;
import com.gabriel.party.services.autenticacao.LimiteDeEnvioDeEmail;
import com.gabriel.party.services.autenticacao.LimiteDeEnvioDeEmail.Envio;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a ligação dos limites no controller, passando pelo GlobalExceptionHandler de verdade:
 * se alguém mover a contagem para dentro do try do login, o catch (Exception) engole o 429 e este teste quebra.
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoControllerTest {

    private static final String LOGIN = "/api/v1/auth/login";
    private static final String RECUPERACAO = "/api/v1/auth/recuperacao-senha";
    private static final String REENVIO = "/api/v1/auth/reenviar-verificacao";
    private static final String IP_DO_MOCKMVC = "127.0.0.1";

    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AutenticacaoService autenticacaoService;
    @Mock private ArmazenamentoService armazenamentoService;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private LimiteDeLogin limiteDeLogin;
    @Mock private LimiteDeEnvioDeEmail limiteDeEnvioDeEmail;

    private MockMvc mvc;

    @BeforeEach
    void montar() {
        var controller = new AutenticacaoController(tokenService, authenticationManager, autenticacaoService,
                armazenamentoService, clienteRepository, prestadorRepository, limiteDeLogin, limiteDeEnvioDeEmail);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("login barrado responde 429 com Retry-After e nem confere a senha")
    void loginBarrado_429() throws Exception {
        doThrow(new MuitasTentativasException(Duration.ofSeconds(90)))
                .when(limiteDeLogin).registrarTentativa(anyString(), anyString());

        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "90"))
                .andExpect(jsonPath("$.code").value("muitas_tentativas"));

        verifyNoInteractions(authenticationManager);
    }

    @Test
    @DisplayName("senha errada responde 401 e não zera os contadores")
    void senhaErrada_401_naoZera() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("senha errada"));

        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());

        verify(limiteDeLogin).registrarTentativa("ana@x.com", IP_DO_MOCKMVC);
        verify(limiteDeLogin, never()).loginComSucesso(anyString(), anyString());
    }

    @Test
    @DisplayName("login certo zera os contadores com o e-mail e o IP de quem entrou")
    void loginCerto_zera() throws Exception {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRole(Role.ROLE_CLIENTE);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null, List.of()));

        mvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\",\"senha\":\"certa\"}"))
                .andExpect(status().isOk());

        verify(limiteDeLogin).loginComSucesso("ana@x.com", IP_DO_MOCKMVC);
    }

    @Test
    @DisplayName("recuperação barrada responde 429 e não envia e-mail")
    void recuperacaoBarrada_429() throws Exception {
        doThrow(new MuitasTentativasException(Duration.ofMinutes(40)))
                .when(limiteDeEnvioDeEmail).registrarPedido(eq(Envio.RECUPERACAO_DE_SENHA), anyString(), anyString());

        mvc.perform(post(RECUPERACAO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "2400"));

        verifyNoInteractions(autenticacaoService);
    }

    @Test
    @DisplayName("recuperação sem e-mail responde 400 (antes estourava NullPointerException e respondia 500)")
    void recuperacaoSemEmail_400() throws Exception {
        mvc.perform(post(RECUPERACAO).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(limiteDeEnvioDeEmail, autenticacaoService);
    }

    @Test
    @DisplayName("reenvio de verificação barrado responde 429 e não envia e-mail")
    void reenvioBarrado_429() throws Exception {
        doThrow(new MuitasTentativasException(Duration.ofMinutes(40)))
                .when(limiteDeEnvioDeEmail).registrarPedido(eq(Envio.VERIFICACAO_DE_EMAIL), anyString(), anyString());

        mvc.perform(post(REENVIO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "2400"));

        verifyNoInteractions(autenticacaoService);
    }

    @Test
    @DisplayName("reenvio de verificação conta no limite próprio e responde 200")
    void reenvio_contaNoLimiteEResponde200() throws Exception {
        mvc.perform(post(REENVIO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@x.com\"}"))
                .andExpect(status().isOk());

        verify(limiteDeEnvioDeEmail).registrarPedido(Envio.VERIFICACAO_DE_EMAIL, "ana@x.com", IP_DO_MOCKMVC);
        verify(autenticacaoService).reenviarVerificacaoEmail("ana@x.com");
    }

    @Test
    @DisplayName("reenvio de verificação sem e-mail responde 400")
    void reenvioSemEmail_400() throws Exception {
        mvc.perform(post(REENVIO).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(limiteDeEnvioDeEmail, autenticacaoService);
    }
}
