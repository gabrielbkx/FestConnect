package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Resultado;
import com.gabriel.party.exceptions.MuitasTentativasException;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimiteDeLoginTest {

    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final String CHAVE_EMAIL = "rl:login:email:ana@x.com";
    private static final String CHAVE_IP = "rl:login:ip:10.0.0.1";

    @Mock private LimitadorDeTentativas limitador;

    @InjectMocks private LimiteDeLogin limiteDeLogin;

    private static Resultado liberado() {
        return new Resultado(true, Duration.ofMinutes(14));
    }

    private static Resultado barrado(Duration falta) {
        return new Resultado(false, falta);
    }

    @Test
    @DisplayName("dentro dos limites, não barra")
    void dentroDoLimite_naoBarra() {
        when(limitador.registrar(anyString(), anyInt(), any())).thenReturn(liberado());

        assertThatCode(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("passou de 5 no e-mail: barra com o tempo que falta para o e-mail")
    void emailEstourado_barra() {
        when(limitador.registrar(CHAVE_EMAIL, 5, JANELA)).thenReturn(barrado(Duration.ofMinutes(3)));
        when(limitador.registrar(CHAVE_IP, 20, JANELA)).thenReturn(liberado());

        assertThatThrownBy(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    @DisplayName("passou de 20 no IP: barra mesmo num e-mail que nunca errou")
    void ipEstourado_barra() {
        when(limitador.registrar(CHAVE_EMAIL, 5, JANELA)).thenReturn(liberado());
        when(limitador.registrar(CHAVE_IP, 20, JANELA)).thenReturn(barrado(Duration.ofMinutes(10)));

        assertThatThrownBy(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("os dois estourados: manda esperar o maior dos tempos")
    void osDoisEstourados_esperaOMaior() {
        when(limitador.registrar(CHAVE_EMAIL, 5, JANELA)).thenReturn(barrado(Duration.ofMinutes(3)));
        when(limitador.registrar(CHAVE_IP, 20, JANELA)).thenReturn(barrado(Duration.ofMinutes(10)));

        assertThatThrownBy(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("e-mail com espaços e maiúsculas conta no mesmo contador")
    void emailNormalizado() {
        when(limitador.registrar(anyString(), anyInt(), any())).thenReturn(liberado());

        limiteDeLogin.registrarTentativa("  Ana@X.com ", "10.0.0.1");

        verify(limitador).registrar(CHAVE_EMAIL, 5, JANELA);
    }

    @Test
    @DisplayName("login com sucesso zera o contador do e-mail, mas não o do IP")
    void sucesso_zeraSoOEmail() {
        limiteDeLogin.loginComSucesso("Ana@X.com");

        verify(limitador).zerar(CHAVE_EMAIL);
        verify(limitador, never()).zerar(startsWith("rl:login:ip:"));
    }
}