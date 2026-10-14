package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimiteDeLoginTest {

    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final Limite EMAIL_E_IP = new Limite("rl:login:email-ip:ana@x.com|10.0.0.1", 5);
    private static final Limite EMAIL = new Limite("rl:login:email:ana@x.com", 30);
    private static final Limite IP = new Limite("rl:login:ip:10.0.0.1", 20);

    @Mock private LimitadorDeTentativas limitador;

    @InjectMocks private LimiteDeLogin limiteDeLogin;

    @Test
    @DisplayName("conta nos três limites: e-mail + IP (5), e-mail (30) e IP (20)")
    void contaNosTresLimites() {
        when(limitador.registrar(any(), any(), any(), any())).thenReturn(new Resultado(true, Duration.ofMinutes(14)));

        limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1");

        verify(limitador).registrar(JANELA, EMAIL_E_IP, EMAIL, IP);
    }

    @Test
    @DisplayName("dentro dos limites, não barra")
    void dentroDoLimite_naoBarra() {
        when(limitador.registrar(any(), any(), any(), any())).thenReturn(new Resultado(true, Duration.ofMinutes(14)));

        assertThatCode(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("algum limite estourado: barra com o tempo que falta")
    void limiteEstourado_barra() {
        when(limitador.registrar(any(), any(), any(), any())).thenReturn(new Resultado(false, Duration.ofMinutes(10)));

        assertThatThrownBy(() -> limiteDeLogin.registrarTentativa("ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("e-mail com espaços e maiúsculas conta no mesmo contador")
    void emailNormalizado() {
        when(limitador.registrar(any(), any(), any(), any())).thenReturn(new Resultado(true, Duration.ofMinutes(14)));

        limiteDeLogin.registrarTentativa("  Ana@X.com ", "10.0.0.1");

        verify(limitador).registrar(JANELA, EMAIL_E_IP, EMAIL, IP);
    }

    @Test
    @DisplayName("IPv6 conta pelo bloco /64, não pelo endereço inteiro")
    void ipv6_contaPeloBloco() {
        when(limitador.registrar(any(), any(), any(), any())).thenReturn(new Resultado(true, Duration.ofMinutes(14)));

        limiteDeLogin.registrarTentativa("ana@x.com", "2001:db8:abcd:12:1111:2222:3333:4444");

        verify(limitador).registrar(JANELA,
                new Limite("rl:login:email-ip:ana@x.com|2001:db8:abcd:12::/64", 5),
                EMAIL,
                new Limite("rl:login:ip:2001:db8:abcd:12::/64", 20));
    }

    @Test
    @DisplayName("login certo zera os contadores da conta e só devolve a tentativa no IP")
    void sucesso_zeraContaEDevolveNoIp() {
        limiteDeLogin.loginComSucesso("Ana@X.com", "10.0.0.1");

        verify(limitador).zerar(EMAIL_E_IP.chave(), EMAIL.chave());
        verify(limitador).devolver(IP.chave());
        verify(limitador, never()).zerar(anyString());   // o IP nunca é zerado: senão o atacante zeraria o próprio limite
    }
}
