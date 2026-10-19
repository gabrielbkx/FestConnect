package com.gabriel.party.services.evento;

import com.gabriel.party.dtos.evento.EventoStatusUpdateDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.evento.EventoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.evento.enums.StatusEvento;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.evento.EventoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock private EventoRepository eventoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private EmailService emailService;
    @Mock private EventoMapper eventoMapper;

    private EventoService service;
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Prestador prestador;
    private Evento evento;

    @BeforeEach
    void setUp() {
        service = new EventoService(eventoRepository, clienteRepository, pedidoRepository, emailService, eventoMapper);

        usuarioCliente = new Usuario();
        usuarioCliente.setId(UUID.randomUUID());

        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setUsuario(usuarioCliente);
        cliente.setNomeCompleto("Cliente Teste");

        var usuarioPrestador = new Usuario();
        usuarioPrestador.setId(UUID.randomUUID());
        usuarioPrestador.setEmail("prestador@test.com");

        prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
        prestador.setUsuario(usuarioPrestador);
        prestador.setNomeCompleto("Prestador Teste");

        evento = new Evento();
        evento.setId(UUID.randomUUID());
        evento.setCliente(cliente);
        evento.setStatus(StatusEvento.PLANEJANDO);

        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(eventoRepository.findById(evento.getId())).thenReturn(Optional.of(evento));
    }

    private Pedido pedidoComStatus(StatusPedido status) {
        var pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);
        pedido.setEvento(evento);
        pedido.setStatusPedido(status);
        pedido.setTipoEvento("Casamento");
        pedido.setLocalEvento("Salão X");
        pedido.setDataEvento(LocalDateTime.now().plusDays(30));
        return pedido;
    }

    // O service busca em duas etapas: primeiro AGUARDANDO_ENTRADA/RESERVADO, depois PENDENTE/ORCADO
    private void mockarPedidosDoEvento(List<Pedido> pedidos) {
        when(pedidoRepository.buscarDoEventoComLock(eq(evento.getId()), anyList())).thenAnswer(inv -> {
            List<StatusPedido> statuses = inv.getArgument(1);
            return pedidos.stream().filter(p -> statuses.contains(p.getStatusPedido())).toList();
        });
    }

    private EventoStatusUpdateDTO cancelar() {
        return new EventoStatusUpdateDTO(StatusEvento.CANCELADO);
    }

    @Test
    @DisplayName("Cancelar evento com pedido RESERVADO deve barrar com 400 e não salvar o evento")
    void cancelarEventoComReservaDeveBarrar() {
        var reservado = pedidoComStatus(StatusPedido.RESERVADO);
        var pendente = pedidoComStatus(StatusPedido.PENDENTE);
        mockarPedidosDoEvento(List.of(pendente, reservado));

        assertThatThrownBy(() -> service.atualizarStatus(evento.getId(), cancelar(), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.EVENTO_COM_RESERVA);

        // Nada fica meio-cancelado: evento intacto e nenhum pedido mexido
        assertThat(evento.getStatus()).isEqualTo(StatusEvento.PLANEJANDO);
        assertThat(pendente.getStatusPedido()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(reservado.getStatusPedido()).isEqualTo(StatusPedido.RESERVADO);
        verify(eventoRepository, never()).save(any());
        verify(emailService, never()).enviarAposCommit(anyString(), anyString(), anyString());
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"PENDENTE", "ORCADO", "AGUARDANDO_ENTRADA"})
    @DisplayName("Cancelar evento deve cancelar pedidos sem sinal pago e avisar o prestador")
    void cancelarEventoDeveCancelarPedidosSemSinal(StatusPedido status) {
        var pedido = pedidoComStatus(status);
        mockarPedidosDoEvento(List.of(pedido));

        service.atualizarStatus(evento.getId(), cancelar(), usuarioCliente);

        assertThat(evento.getStatus()).isEqualTo(StatusEvento.CANCELADO);
        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
        verify(eventoRepository).save(evento);
        verify(emailService).enviarAposCommit(eq("prestador@test.com"), eq("Resenha - Evento cancelado"), anyString());
    }

    @Test
    @DisplayName("Cancelar evento deve travar AGUARDANDO_ENTRADA/RESERVADO antes de PENDENTE/ORCADO")
    void cancelarEventoDeveTravarNaOrdemCerta() {
        var orcado = pedidoComStatus(StatusPedido.ORCADO);
        var aguardando = pedidoComStatus(StatusPedido.AGUARDANDO_ENTRADA);
        mockarPedidosDoEvento(List.of(orcado, aguardando));

        service.atualizarStatus(evento.getId(), cancelar(), usuarioCliente);

        ArgumentCaptor<List<StatusPedido>> statuses = ArgumentCaptor.captor();
        InOrder ordem = inOrder(pedidoRepository);
        ordem.verify(pedidoRepository, times(2)).buscarDoEventoComLock(eq(evento.getId()), statuses.capture());
        assertThat(statuses.getAllValues().get(0))
                .containsExactlyInAnyOrder(StatusPedido.AGUARDANDO_ENTRADA, StatusPedido.RESERVADO);
        assertThat(statuses.getAllValues().get(1))
                .containsExactlyInAnyOrder(StatusPedido.PENDENTE, StatusPedido.ORCADO);

        assertThat(orcado.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(aguardando.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
        verify(emailService, times(2)).enviarAposCommit(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Mudar status para algo diferente de CANCELADO não deve mexer nos pedidos")
    void confirmarEventoNaoMexeNosPedidos() {
        service.atualizarStatus(evento.getId(), new EventoStatusUpdateDTO(StatusEvento.CONFIRMADO), usuarioCliente);

        assertThat(evento.getStatus()).isEqualTo(StatusEvento.CONFIRMADO);
        verify(eventoRepository).save(evento);
        verify(pedidoRepository, never()).buscarDoEventoComLock(any(), anyList());
        verify(emailService, never()).enviarAposCommit(anyString(), anyString(), anyString());
    }
}
