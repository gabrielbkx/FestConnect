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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimiteDeRecuperacaoDeSenhaTest {

    private static final Duration JANELA = Duration.ofHours(1);
    private static final String CHAVE_EMAIL = "rl:recuperacao:email:ana@x.com";
    private static final String CHAVE_IP = "rl:recuperacao:ip:10.0.0.1";

    @Mock private LimitadorDeTentativas limitador;

    @InjectMocks private LimiteDeRecuperacaoDeSenha limite;

    private static Resultado liberado() {
        return new Resultado(true, Duration.ofMinutes(50));
    }

    private static Resultado barrado(Duration falta) {
        return new Resultado(false, falta);
    }

    @Test
    @DisplayName("dentro dos limites, não barra")
    void dentroDoLimite_naoBarra() {
        when(limitador.registrar(anyString(), anyInt(), any())).thenReturn(liberado());

        assertThatCode(() -> limite.registrarPedido("ana@x.com", "10.0.0.1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("passou de 3 pedidos no e-mail em 1 hora: barra")
    void emailEstourado_barra() {
        when(limitador.registrar(CHAVE_EMAIL, 3, JANELA)).thenReturn(barrado(Duration.ofMinutes(40)));
        when(limitador.registrar(CHAVE_IP, 10, JANELA)).thenReturn(liberado());

        assertThatThrownBy(() -> limite.registrarPedido("ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(40));
    }

    @Test
    @DisplayName("passou de 10 pedidos no IP em 1 hora: barra mesmo num e-mail novo")
    void ipEstourado_barra() {
        when(limitador.registrar(CHAVE_EMAIL, 3, JANELA)).thenReturn(liberado());
        when(limitador.registrar(CHAVE_IP, 10, JANELA)).thenReturn(barrado(Duration.ofMinutes(25)));

        assertThatThrownBy(() -> limite.registrarPedido("ana@x.com", "10.0.0.1"))
                .isInstanceOf(MuitasTentativasException.class)
                .extracting("tempoRestante").isEqualTo(Duration.ofMinutes(25));
    }

    @Test
    @DisplayName("e-mail com espaços e maiúsculas conta no mesmo contador")
    void emailNormalizado() {
        when(limitador.registrar(anyString(), anyInt(), any())).thenReturn(liberado());

        limite.registrarPedido("  Ana@X.com ", "10.0.0.1");

        verify(limitador).registrar(CHAVE_EMAIL, 3, JANELA);
    }
}
