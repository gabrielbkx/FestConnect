package com.gabriel.party.services.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.pedido.PedidoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private PedidoService service;

    private UUID usuarioId;
    private UUID prestadorId;
    private UUID clienteId;
    private UUID pedidoId;
    private Usuario usuarioCliente;
    private Usuario usuarioPrestador;
    private Cliente cliente;
    private Prestador prestador;
    private Pedido pedido;
    private PedidoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        prestadorId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();

        usuarioCliente = new Usuario();
        usuarioCliente.setId(usuarioId);
        usuarioCliente.setEmail("cliente@teste.com");
        usuarioCliente.setRole(Role.ROLE_CLIENTE);

        usuarioPrestador = new Usuario();
        usuarioPrestador.setId(UUID.randomUUID());
        usuarioPrestador.setEmail("prestador@teste.com");
        usuarioPrestador.setRole(Role.ROLE_PRESTADOR);

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setFotoPerfilUrl("https://foto.jpg");

        prestador = new Prestador();
        prestador.setId(prestadorId);
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setUsuario(usuarioPrestador);

        pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setDataEvento(LocalDateTime.now().plusDays(30));
        pedido.setLocalEvento("Salão de Festas");
        pedido.setTipoEvento("Casamento");
        pedido.setNumeroConvidados(100);
        pedido.setDescricao("Festa de casamento");
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);

        responseDTO = new PedidoResponseDTO(
                pedidoId, "Cliente Teste", "https://foto.jpg",
                LocalDateTime.now().plusDays(30), "Salão de Festas",
                "Casamento", 100, "Festa de casamento", StatusPedido.PENDENTE
        );
    }

    @Nested
    @DisplayName("criarPedido")
    class CriarPedido {

        @Test
        @DisplayName("Deve criar pedido com sucesso")
        void deveCriarPedidoComSucesso() {
            var dto = new PedidoRequestDTO(prestadorId, LocalDateTime.now().plusDays(30),
                    "Salão de Festas", "Casamento", 100, "Festa de casamento");

            when(clienteRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findById(prestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoMapper.toEntity(dto, cliente, prestador)).thenReturn(pedido);
            when(pedidoRepository.save(pedido)).thenReturn(pedido);

            Pedido resultado = service.criarPedido(dto, usuarioCliente);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getId()).isEqualTo(pedidoId);
            verify(pedidoRepository).save(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            var dto = new PedidoRequestDTO(prestadorId, LocalDateTime.now().plusDays(30),
                    "Local", "Tipo", 50, "Desc");

            when(clienteRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarPedido(dto, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cliente não encontrado");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            var dto = new PedidoRequestDTO(prestadorId, LocalDateTime.now().plusDays(30),
                    "Local", "Tipo", 50, "Desc");

            when(clienteRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findById(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarPedido(dto, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Prestador não encontrado");

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarPedidosPendentes")
    class ListarPedidosPendentes {

        @Test
        @DisplayName("Deve listar pedidos pendentes do prestador")
        void deveListarPedidosPendentes() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(prestador));
            when(pedidoRepository.findByPrestadorIdAndStatusPedido(prestadorId, StatusPedido.PENDENTE))
                    .thenReturn(List.of(pedido));
            when(pedidoMapper.toResponseList(List.of(pedido))).thenReturn(List.of(responseDTO));

            var resultado = service.listarPedidosPendentes(usuarioPrestador);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).status()).isEqualTo(StatusPedido.PENDENTE);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há pedidos pendentes")
        void deveRetornarListaVazia() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(prestador));
            when(pedidoRepository.findByPrestadorIdAndStatusPedido(prestadorId, StatusPedido.PENDENTE))
                    .thenReturn(List.of());
            when(pedidoMapper.toResponseList(List.of())).thenReturn(List.of());

            var resultado = service.listarPedidosPendentes(usuarioPrestador);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarPedidosPendentes(usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("enviarOrcamento")
    class EnviarOrcamento {

        @Test
        @DisplayName("Deve enviar orçamento com sucesso")
        void deveEnviarOrcamentoComSucesso() {
            var dto = new OrcamentoRequestDTO(new BigDecimal("5000.00"),
                    "Inclui decoração, buffet e DJ", LocalDateTime.now().plusDays(7));

            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.save(pedido)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

            var resultado = service.enviarOrcamento(pedidoId, dto, usuarioPrestador);

            assertThat(resultado).isNotNull();
            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ORCADO);
            verify(pedidoMapper).updatePedidoFromOrcamento(dto, pedido);
            verify(pedidoRepository).save(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado ao enviar orçamento")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            var dto = new OrcamentoRequestDTO(new BigDecimal("5000.00"),
                    "Detalhes", LocalDateTime.now().plusDays(7));

            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, dto, usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado ao enviar orçamento")
        void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
            var dto = new OrcamentoRequestDTO(new BigDecimal("5000.00"),
                    "Detalhes", LocalDateTime.now().plusDays(7));

            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, dto, usuarioPrestador))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Pedido não encontrado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não é o dono do pedido")
        void deveLancarExcecaoQuandoPrestadorNaoEDono() {
            var dto = new OrcamentoRequestDTO(new BigDecimal("5000.00"),
                    "Detalhes", LocalDateTime.now().plusDays(7));

            var outroPrestador = new Prestador();
            outroPrestador.setId(UUID.randomUUID());

            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(outroPrestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, dto, usuarioPrestador))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("permissão");
        }
    }

    @Nested
    @DisplayName("recusarPedido")
    class RecusarPedido {

        @Test
        @DisplayName("Deve recusar pedido com sucesso")
        void deveRecusarPedidoComSucesso() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            service.recusarPedido(pedidoId, usuarioPrestador);

            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.RECUSADO);
            verify(pedidoRepository).save(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não é o dono ao recusar")
        void deveLancarExcecaoQuandoPrestadorNaoEDono() {
            var outroPrestador = new Prestador();
            outroPrestador.setId(UUID.randomUUID());

            when(prestadorRepository.findByUsuarioId(usuarioPrestador.getId()))
                    .thenReturn(Optional.of(outroPrestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.recusarPedido(pedidoId, usuarioPrestador))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("permissão");

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("aceitarOrcamento")
    class AceitarOrcamento {

        @Test
        @DisplayName("Deve aceitar orçamento com sucesso")
        void deveAceitarOrcamentoComSucesso() {
            pedido.setStatusPedido(StatusPedido.ORCADO);

            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.save(pedido)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

            var resultado = service.aceitarOrcamento(pedidoId, usuarioCliente);

            assertThat(resultado).isNotNull();
            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ACEITO);
            verify(pedidoRepository).save(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não está orçado")
        void deveLancarExcecaoQuandoPedidoNaoEstaOrcado() {
            pedido.setStatusPedido(StatusPedido.PENDENTE);

            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("orçados");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não é o dono do pedido ao aceitar")
        void deveLancarExcecaoQuandoClienteNaoEDono() {
            pedido.setStatusPedido(StatusPedido.ORCADO);

            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());

            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(outroCliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("permissão");
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado ao aceitar")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("cancelarPedidoPeloCliente")
    class CancelarPedidoPeloCliente {

        @Test
        @DisplayName("Deve cancelar pedido com sucesso")
        void deveCancelarPedidoComSucesso() {
            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente);

            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
            verify(pedidoRepository).save(pedido);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não é o dono ao cancelar")
        void deveLancarExcecaoQuandoClienteNaoEDono() {
            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());

            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(outroCliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("permissão");

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado ao cancelar")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cliente não encontrado");
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado ao cancelar")
        void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioCliente.getId()))
                    .thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Pedido não encontrado");
        }
    }
}
