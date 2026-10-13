package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.DadosDeValidacaoDeCodigoRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRecuperacaoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.autenticacao.CodigoRecuperacaoRepository;
import com.gabriel.party.services.email.EmailService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    private static final String EMAIL = "cliente@festconnect.com";
    private static final String PIN_CORRETO = "123456";
    private static final String PIN_ERRADO = "999999";

    // Injeção por campo: só precisamos mockar o que os métodos testados usam
    @Mock private CodigoRecuperacaoRepository codigoRecuperacaoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenService tokenService;
    @Mock private EmailService emailService;

    @InjectMocks private AutenticacaoService service;

    private Usuario usuario;
    private CodigoRecuperacao codigo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail(EMAIL);

        codigo = new CodigoRecuperacao();
        codigo.setId(UUID.randomUUID());
        codigo.setCodigoPin(PIN_CORRETO);
        codigo.setDataCriacao(LocalDateTime.now().minusMinutes(2));
        codigo.setDataExpiracao(LocalDateTime.now().plusMinutes(13));
        codigo.setTentativas(0);
        codigo.setUsuario(usuario);
    }

    private DadosDeValidacaoDeCodigoRecuperacaoDTO tentativa(String pin) {
        return new DadosDeValidacaoDeCodigoRecuperacaoDTO(EMAIL, pin);
    }

    // ── Validação do PIN

    @Test
    @DisplayName("e-mail sem código ativo recebe o mesmo erro de PIN errado (e não 500)")
    void validarCodigo_semCodigoAtivo_lancaCodigoInvalido() {
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.empty());

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_CORRETO)));

        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("PIN errado soma uma tentativa e mantém o código")
    void validarCodigo_pinErrado_incrementaTentativas() {
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_ERRADO)));

        assertThat(codigo.getTentativas()).isEqualTo(1);
        verify(codigoRecuperacaoRepository).save(codigo);
        verify(codigoRecuperacaoRepository, never()).delete(any());
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("o 5º PIN errado apaga o código")
    void validarCodigo_quintoErro_apagaCodigo() {
        codigo.setTentativas(AutenticacaoService.MAX_TENTATIVAS_PIN - 1);
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_ERRADO)));

        verify(codigoRecuperacaoRepository).delete(codigo);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("depois de 5 erros, a 6ª tentativa é recusada mesmo com o PIN certo")
    void validarCodigo_seisTentativas_pinCertoRecusadoDepoisDoLimite() {
        // "Banco" de mentira com estado: o código existe até alguém apagá-lo
        boolean[] apagado = {false};
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL))
                .thenAnswer(inv -> apagado[0] ? Optional.empty() : Optional.of(codigo));
        doAnswer(inv -> apagado[0] = true).when(codigoRecuperacaoRepository).delete(codigo);

        for (int i = 0; i < AutenticacaoService.MAX_TENTATIVAS_PIN; i++) {
            assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_ERRADO)));
        }

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_CORRETO)));
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("PIN certo apaga o código e devolve o token de redefinição")
    void validarCodigo_pinCerto_apagaCodigoEGeraToken() {
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));
        when(tokenService.gerarTokenParaRecuperacaoDeSenha(usuario)).thenReturn("token-redefinicao");

        String token = service.validarCodigoRecuperacao(tentativa(PIN_CORRETO));

        assertThat(token).isEqualTo("token-redefinicao");
        verify(codigoRecuperacaoRepository).delete(codigo);
    }

    @Test
    @DisplayName("código expirado é recusado e apagado, mesmo com o PIN certo")
    void validarCodigo_expirado_apagaERecusa() {
        codigo.setDataExpiracao(LocalDateTime.now().minusSeconds(1));
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(PIN_CORRETO)));

        verify(codigoRecuperacaoRepository).delete(codigo);
        verifyNoInteractions(tokenService);
    }

    @Test
    @DisplayName("PIN nulo conta como errado, sem estourar NullPointerException")
    void validarCodigo_pinNulo_contaComoErrado() {
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        assertCodigoInvalido(() -> service.validarCodigoRecuperacao(tentativa(null)));

        assertThat(codigo.getTentativas()).isEqualTo(1);
    }

    // ── Envio do código

    @Test
    @DisplayName("e-mail sem cadastro: não gera código nem envia nada (e a resposta continua 200)")
    void enviarCodigo_emailInexistente_naoFazNada() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        service.enviarCodigoRecuperacao(new DadosRecuperacaoDTO(EMAIL));

        verifyNoInteractions(codigoRecuperacaoRepository, emailService);
    }

    @Test
    @DisplayName("pedir de novo apaga o código anterior ANTES de salvar o novo")
    void enviarCodigo_apagaAnteriorAntesDeSalvar() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.empty());

        service.enviarCodigoRecuperacao(new DadosRecuperacaoDTO(EMAIL));

        // A ordem importa: com a restrição de um código por usuário, salvar antes de apagar quebraria
        InOrder ordem = inOrder(codigoRecuperacaoRepository);
        ordem.verify(codigoRecuperacaoRepository).deleteByUsuario(usuario);
        ordem.verify(codigoRecuperacaoRepository).save(any(CodigoRecuperacao.class));
    }

    @Test
    @DisplayName("o código salvo tem PIN de 6 dígitos, zero tentativas e validade de 15 minutos; o PIN vai no e-mail")
    void enviarCodigo_salvaCodigoNovoEEnviaPin() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.empty());

        service.enviarCodigoRecuperacao(new DadosRecuperacaoDTO(EMAIL));

        // O ArgumentCaptor "pega" o objeto que o service passou para o save
        ArgumentCaptor<CodigoRecuperacao> captor = ArgumentCaptor.forClass(CodigoRecuperacao.class);
        verify(codigoRecuperacaoRepository).save(captor.capture());
        CodigoRecuperacao salvo = captor.getValue();

        assertThat(salvo.getCodigoPin()).matches("\\d{6}");
        assertThat(salvo.getTentativas()).isZero();
        assertThat(salvo.getUsuario()).isSameAs(usuario);
        assertThat(salvo.getDataExpiracao())
                .isCloseTo(salvo.getDataCriacao().plusMinutes(AutenticacaoService.VALIDADE_CODIGO_MINUTOS), within(1, SECONDS));
        verify(emailService).enviarAposCommit(eq(EMAIL), anyString(), contains(salvo.getCodigoPin()));
    }

    @Test
    @DisplayName("reenvio antes de 60 s é ignorado: não apaga, não salva, não manda e-mail")
    void enviarCodigo_reenvioCedoDemais_ignora() {
        codigo.setDataCriacao(LocalDateTime.now().minusSeconds(10));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        service.enviarCodigoRecuperacao(new DadosRecuperacaoDTO(EMAIL));

        verify(codigoRecuperacaoRepository, never()).deleteByUsuario(any());
        verify(codigoRecuperacaoRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("reenvio depois de 60 s gera um código novo")
    void enviarCodigo_reenvioDepoisDoIntervalo_geraNovo() {
        codigo.setDataCriacao(LocalDateTime.now().minusSeconds(AutenticacaoService.INTERVALO_MINIMO_REENVIO_SEGUNDOS + 1));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRecuperacaoRepository.findByUsuarioEmail(EMAIL)).thenReturn(Optional.of(codigo));

        service.enviarCodigoRecuperacao(new DadosRecuperacaoDTO(EMAIL));

        verify(codigoRecuperacaoRepository).deleteByUsuario(usuario);
        verify(codigoRecuperacaoRepository).save(any(CodigoRecuperacao.class));
        verify(emailService).enviarAposCommit(eq(EMAIL), anyString(), anyString());
    }

    private void assertCodigoInvalido(ThrowingCallable acao) {
        assertThatThrownBy(acao)
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO);
    }
}
