package com.gabriel.party.services.pagamento.gateway;

import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FakeGatewayTest {

    private final FakeGateway gateway = new FakeGateway();

    @Test
    @DisplayName("identifica-se como FAKE")
    void nome_ehFake() {
        assertThat(gateway.nome()).isEqualTo("FAKE");
    }

    @Test
    @DisplayName("aprova a cobrança na hora e devolve id externo e comprovante")
    void cobrar_aprovaNaHora() {
        var resultado = gateway.cobrar(new DadosCobranca(new BigDecimal("750.00"), "Sinal do pedido", UUID.randomUUID()));

        assertThat(resultado.status()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(resultado.idExterno()).startsWith("fake_");
        assertThat(resultado.urlComprovante()).contains(resultado.idExterno());
    }
}
