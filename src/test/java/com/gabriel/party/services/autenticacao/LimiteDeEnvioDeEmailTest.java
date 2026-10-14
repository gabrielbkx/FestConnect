package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Resultado;
import com.gabriel.party.exceptions.MuitasTentativasException;
import com.gabriel.party.services.autenticacao.LimiteDeEnvioDeEmail.Envio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimiteDeEnvioDeEmailTest {

    private static final Duration JANELA = Duration.ofHours(1);

    @Mock private LimitadorDeTentativas limitador;

    @InjectMocks private LimiteDeEnvioDeEmail limite;

    private void liberarTudo() {
        when(limitador.registrar(any(Duration.class), any(Limite.class), any(Limite.class)))
                .thenReturn(new Resultado(true, Duration.ofMinutes(50)));
    }

    @Test
    @DisplayName("recuperação de senha conta 3 por e-mail e 10 por IP na janela de 1 hora")
    void recuperacao_contaNosDoisLimites() {
        liberarTudo();

        limite.registrarPedido(Envio.RECUPERACAO_DE_SENHA, "ana@x.com", "10.0.0.1");

        verify(limitador).registrar(JANELA,
                new Limite("rl:recuperacao:email:ana@x.com", 3),
                new Limite("rl:recuperacao:ip:10.0.0.1", 10));
    }

    @Test
    @DisplayName("reenvio de verificação tem contadores próprios, separados dos da recuperação")
    void verificacao_contadoresProprios() {
        liberarTudo();

        limite.registrarPedido(Envio.VERIFICACAO_DE_EMAIL, "ana@x.com", "10.0.0.1");

        verify(limitador).registrar(JANELA,
                new Limite("rl:verificacao:email:ana@x.com", 3),
                new Limite("rl:verificacao:ip:10.0.0.1", 10));
    }

    @Test
    @DisplayName("dentro dos limites, não barra")
    void dentroDoLimite_naoBarra() {
        liberarTudo();

        assertThatCode(() -> limite.registrarPedido(Envio.RECUPERACAO_DE_SENHA, "ana@x.com", "10.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("algum limite estourado: barra com o tempo que falta")
    void limiteEstourado_barra() {
        when(limitador.registrar(any(Duration.class), any(Limite.class), any(Limite.class)))
                .thenReturn(new Resultado(false, Duration.ofMinutes(40)));

        assertThatThrownBy(() -> limite.registrarPedido(Envio.VERIFICACAO_DE_EMAIL, "ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(40));
    }

    @Test
    @DisplayName("e-mail com espaços e maiúsculas conta no mesmo contador")
    void emailNormalizado() {
        liberarTudo();

        limite.registrarPedido(Envio.RECUPERACAO_DE_SENHA, "  Ana@X.com ", "10.0.0.1");

        verify(limitador).registrar(JANELA,
                new Limite("rl:recuperacao:email:ana@x.com", 3),
                new Limite("rl:recuperacao:ip:10.0.0.1", 10));
    }
}
