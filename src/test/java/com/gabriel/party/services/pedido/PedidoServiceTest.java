package com.gabriel.party.services.pedido;


import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.mapper.pedido.PedidoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.evento.EventoRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private ItemCatalogoRepository itemCatalogoRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private PedidoMapper pedidoMapper;
    @Mock private EmailService emailService;

    private PedidoService service;
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Prestador prestador;
    private ItemCatalogo item;

    @BeforeEach
    void setUp() {
        // Injeção via construtor nos dá controle das 72h
        service = new PedidoService(pedidoRepository, clienteRepository, prestadorRepository,
                itemCatalogoRepository, eventoRepository, pedidoMapper, emailService, 72L);

        usuarioCliente = new Usuario();
        usuarioCliente.setId(UUID.randomUUID());

        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());
        cliente.setUsuario(usuarioCliente);

        var usuarioPrestador = new Usuario();
        usuarioPrestador.setId(UUID.randomUUID());
        usuarioPrestador.setEmail("prestador@test.com");

        prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
        prestador.setUsuario(usuarioPrestador);
        cliente.setNomeCompleto("Cliente Teste");
        prestador.setNomeCompleto("Prestador Teste");

        item = new Servico();
        item.setId(UUID.randomUUID());
        item.setPrestador(prestador);
    }

    private PedidoRequestDTO novoRequest(LocalDateTime dataEvento) {
        return new PedidoRequestDTO(
                prestador.getId(), item.getId(), dataEvento,
                "Salão X", "Casamento", 100, "Buffet completo", null);
    }

    private void mockarFluxoCriacao(LocalDateTime dataEvento) {
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(prestadorRepository.findById(prestador.getId())).thenReturn(Optional.of(prestador));
        when(itemCatalogoRepository.findByIdAndAtivoTrue(item.getId())).thenReturn(Optional.of(item));

        var pedidoEntity = new Pedido();
        pedidoEntity.setCliente(cliente);
        pedidoEntity.setPrestador(prestador);
        pedidoEntity.setItemCatalogo(item);
        pedidoEntity.setDataEvento(dataEvento);
        pedidoEntity.setTipoEvento("Casamento");
        pedidoEntity.setLocalEvento("Salão X");
        pedidoEntity.setNumeroConvidados(100);
        when(pedidoMapper.toEntity(any(), any(), any(), any(), any())).thenReturn(pedidoEntity);
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("criarPedido trunca o prazo de resposta ao evento quando o evento é antes de 72h")
    void criarPedido_truncaPrazoAoEvento() {
        LocalDateTime dataEvento = LocalDateTime.now().plusHours(10);
        mockarFluxoCriacao(dataEvento);

        service.criarPedido(novoRequest(dataEvento), usuarioCliente);

        var captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertThat(captor.getValue().getPrazoResposta()).isEqualTo(dataEvento);
    }

    @Test
    @DisplayName("criarPedido usa agora + 72h quando o evento é distante")
    void criarPedido_usaJanelaPadraoQuandoEventoDistante() {
        LocalDateTime dataEvento = LocalDateTime.now().plusDays(30);
        mockarFluxoCriacao(dataEvento);

        service.criarPedido(novoRequest(dataEvento), usuarioCliente);

        var captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        LocalDateTime prazo = captor.getValue().getPrazoResposta();
        assertThat(prazo).isBefore(dataEvento);
        assertThat(prazo).isAfter(LocalDateTime.now().plusHours(71));
        assertThat(prazo).isBefore(LocalDateTime.now().plusHours(73));
    }
}