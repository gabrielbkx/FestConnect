package com.gabriel.party.services.pagamento.gateway;

import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Gateway de mentira para desenvolvimento local: aprova a cobrança na hora, sem sair da máquina. É o
 * que permite exercitar o fluxo de sinal/reserva de ponta a ponta sem contratar provedor (#99). Quando
 * uma intermediadora real entrar, ela vira outra implementação de {@link PagamentoGateway} e a escolha
 * passa a ser por configuração; por ora este é o único bean.
 */
@Component
public class FakeGateway implements PagamentoGateway {

    @Override
    public String nome() {
        return "FAKE";
    }

    @Override
    public ResultadoCobranca cobrar(DadosCobranca dados) {
        String idExterno = "fake_" + UUID.randomUUID();
        return new ResultadoCobranca(idExterno, StatusPagamento.APROVADO, "https://pagamento.local/fake/" + idExterno);
    }
}
