package com.gabriel.party.services.pedido;

import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoExpiracaoJobTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private PedidoExpiracaoJob scheduler;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        var usuarioCliente = new Usuario();
        usuarioCliente.setId(UUID.randomUUID());
        usuarioCliente.setEmail("cliente@test.com");

        var cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setUsuario(usuarioCliente);

        var usuarioPrestador = new Usuario();
        usuarioPrestador.setId(UUID.randomUUID());
        usuarioPrestador.setEmail("prestador@test.com");

        var prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setUsuario(usuarioPrestador);

        pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);
        pedido.setStatusPedido(StatusPedido.ORCADO);
        pedido.setDataEvento(LocalDateTime.now().plusDays(30));
        pedido.setLocalEvento("Salão de Festas");
        pedido.setTipoEvento("Casamento");
        pedido.setNumeroConvidados(100);
        pedido.setDescricao("Buffet completo");
        pedido.setValidadeOrcamento(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Deve expirar orçamentos vencidos, salvar e notificar clientes")
    void deveExpirarOrcamentosVencidos() {
        when(pedidoRepository.findByStatusPedidoAndValidadeOrcamentoBefore(eq(StatusPedido.ORCADO), any()))
                .thenReturn(List.of(pedido));

        scheduler.expirarPedidosVencidos();

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.EXPIRADO);
        verify(pedidoRepository).save(pedido);
        verify(emailService).enviarEmail(eq("cliente@test.com"), any(), any());
    }

    @Test
    @DisplayName("Deve não fazer nada quando não há orçamentos vencidos")
    void naoDeveFazerNadaQuandoNaoHaOrcamentosVencidos() {
        when(pedidoRepository.findByStatusPedidoAndValidadeOrcamentoBefore(eq(StatusPedido.ORCADO), any()))
                .thenReturn(List.of());

        scheduler.expirarPedidosVencidos();

        verify(pedidoRepository, never()).save(any());
        verify(emailService, never()).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Deve expirar múltiplos orçamentos e enviar email para cada cliente")
    void deveExpirarMultiplosOrcamentos() {
        var segundoPedido = new Pedido();
        segundoPedido.setId(UUID.randomUUID());
        segundoPedido.setCliente(pedido.getCliente());
        segundoPedido.setPrestador(pedido.getPrestador());
        segundoPedido.setStatusPedido(StatusPedido.ORCADO);
        segundoPedido.setDataEvento(LocalDateTime.now().plusDays(60));
        segundoPedido.setLocalEvento("Clube");
        segundoPedido.setTipoEvento("Aniversário");
        segundoPedido.setNumeroConvidados(50);
        segundoPedido.setDescricao("DJ e buffet");
        segundoPedido.setValidadeOrcamento(LocalDateTime.now().minusDays(1));

        when(pedidoRepository.findByStatusPedidoAndValidadeOrcamentoBefore(eq(StatusPedido.ORCADO), any()))
                .thenReturn(List.of(pedido, segundoPedido));

        scheduler.expirarPedidosVencidos();

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.EXPIRADO);
        assertThat(segundoPedido.getStatusPedido()).isEqualTo(StatusPedido.EXPIRADO);
        verify(pedidoRepository, times(2)).save(any());
        verify(emailService, times(4)).enviarEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Deve expirar pedidos PENDENTE sem resposta e notificar cliente e prestador")
    void deveExpirarPendentesSemResposta() {
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setValidadeOrcamento(null);
        pedido.setPrazoResposta(LocalDateTime.now().minusHours(1));

        when(pedidoRepository.findByStatusPedidoAndPrazoRespostaBefore(eq(StatusPedido.PENDENTE), any()))
                .thenReturn(List.of(pedido));

        scheduler.expirarPedidosVencidos();

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.EXPIRADO);
        verify(pedidoRepository).save(pedido);
        verify(emailService).enviarEmail(eq("cliente@test.com"), any(), any());
        verify(emailService).enviarEmail(eq("prestador@test.com"), any(), any());
    }
}
