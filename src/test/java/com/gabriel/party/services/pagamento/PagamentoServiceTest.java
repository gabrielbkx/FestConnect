package com.gabriel.party.services.pagamento;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.pagamento.PagamentoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.pagamento.gateway.DadosCobranca;
import com.gabriel.party.services.pagamento.gateway.PagamentoGateway;
import com.gabriel.party.services.pagamento.gateway.ResultadoCobranca;
import com.gabriel.party.services.pedido.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock private PagamentoRepository pagamentoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PagamentoGateway gateway;
    @Mock private PedidoService pedidoService;

    private PagamentoService service;

    private Usuario usuarioCliente;
    private Cliente cliente;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        service = new PagamentoService(pagamentoRepository, pedidoRepository, clienteRepository, gateway, pedidoService);

        usuarioCliente = new Usuario();
        usuarioCliente.setId(UUID.randomUUID());

        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setUsuario(usuarioCliente);

        pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_ENTRADA);
        pedido.setValorSinal(new BigDecimal("750.00"));
        pedido.setPrazoEntrada(LocalDateTime.now().plusHours(72));

        lenient().when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        lenient().when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        lenient().when(pagamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(gateway.nome()).thenReturn("FAKE");
    }

    @Test
    @DisplayName("sinal aprovado registra o pagamento e manda reservar a data")
    void pagarSinal_aprovado_reserva() {
        when(gateway.cobrar(any(DadosCobranca.class)))
                .thenReturn(new ResultadoCobranca("fake_1", StatusPagamento.APROVADO, "https://pagamento.local/fake/fake_1"));
        doAnswer(inv -> {
            pedido.setStatusPedido(StatusPedido.RESERVADO);
            return null;
        }).when(pedidoService).reservarAposSinalAprovado(pedido.getId());

        var resposta = service.pagarSinal(pedido.getId(), usuarioCliente);

        assertThat(resposta.statusPagamento()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(resposta.statusPedido()).isEqualTo(StatusPedido.RESERVADO);
        assertThat(resposta.valor()).isEqualByComparingTo("750.00");
        verify(pedidoService).reservarAposSinalAprovado(pedido.getId());
        verify(pagamentoRepository).save(any());
    }

    @Test
    @DisplayName("sinal recusado registra o pagamento e NÃO reserva; o pedido fica aguardando")
    void pagarSinal_recusado_naoReserva() {
        when(gateway.cobrar(any(DadosCobranca.class)))
                .thenReturn(new ResultadoCobranca("fake_2", StatusPagamento.RECUSADO, null));

        var resposta = service.pagarSinal(pedido.getId(), usuarioCliente);

        assertThat(resposta.statusPagamento()).isEqualTo(StatusPagamento.RECUSADO);
        assertThat(resposta.statusPedido()).isEqualTo(StatusPedido.AGUARDANDO_ENTRADA);
        verify(pedidoService, never()).reservarAposSinalAprovado(any());
        verify(pagamentoRepository).save(any());
    }

    @Test
    @DisplayName("só paga o sinal de um pedido AGUARDANDO_ENTRADA")
    void pagarSinal_statusInvalido_barra() {
        pedido.setStatusPedido(StatusPedido.ORCADO);

        assertThatThrownBy(() -> service.pagarSinal(pedido.getId(), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_STATUS_INVALIDO);
        verify(gateway, never()).cobrar(any());
        verify(pedidoService, never()).reservarAposSinalAprovado(any());
    }

    @Test
    @DisplayName("sinal com prazo vencido é barrado")
    void pagarSinal_prazoVencido_barra() {
        pedido.setPrazoEntrada(LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> service.pagarSinal(pedido.getId(), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_PRAZO_EXPIRADO);
        verify(gateway, never()).cobrar(any());
    }

    @Test
    @DisplayName("cliente não dono do pedido não paga o sinal")
    void pagarSinal_naoDono_barra() {
        var outro = new Cliente();
        outro.setId(UUID.randomUUID());
        pedido.setCliente(outro);

        assertThatThrownBy(() -> service.pagarSinal(pedido.getId(), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_SEM_PERMISSAO);
        verify(gateway, never()).cobrar(any());
    }
}
