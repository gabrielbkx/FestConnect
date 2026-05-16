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
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.email.EmailService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private PedidoMapper pedidoMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private PedidoService service;

    private UUID usuarioClienteId;
    private UUID usuarioPrestadorId;
    private UUID pedidoId;
    private UUID prestadorId;
    private UUID clienteId;

    private Usuario usuarioCliente;
    private Usuario usuarioPrestador;
    private Cliente cliente;
    private Prestador prestador;
    private Pedido pedido;
    private PedidoResponseDTO responseDTO;
    private PedidoRequestDTO requestDTO;
    private OrcamentoRequestDTO orcamentoDTO;

    @BeforeEach
    void setUp() {
        usuarioClienteId = UUID.randomUUID();
        usuarioPrestadorId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();
        prestadorId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        usuarioCliente = new Usuario();
        usuarioCliente.setId(usuarioClienteId);
        usuarioCliente.setEmail("cliente@test.com");

        usuarioPrestador = new Usuario();
        usuarioPrestador.setId(usuarioPrestadorId);
        usuarioPrestador.setEmail("prestador@test.com");

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNomeCompleto("Cliente Teste");
        cliente.setUsuario(usuarioCliente);

        prestador = new Prestador();
        prestador.setId(prestadorId);
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setUsuario(usuarioPrestador);

        pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setDataEvento(LocalDateTime.now().plusDays(30));
        pedido.setLocalEvento("Salão de Festas");
        pedido.setTipoEvento("Casamento");
        pedido.setNumeroConvidados(100);
        pedido.setDescricao("Preciso de buffet completo");
        pedido.setValor(new BigDecimal("2500.00"));
        pedido.setValidadeOrcamento(LocalDateTime.now().plusDays(7));

        requestDTO = new PedidoRequestDTO(
                prestadorId,
                LocalDateTime.now().plusDays(30),
                "Salão de Festas",
                "Casamento",
                100,
                "Preciso de buffet completo"
        );

        orcamentoDTO = new OrcamentoRequestDTO(
                new BigDecimal("2500.00"),
                "Inclui buffet completo para 100 pessoas",
                LocalDateTime.now().plusDays(7)
        );

        responseDTO =
                new PedidoResponseDTO(
                pedidoId, "Cliente Teste", null, "Prestador Teste",
                LocalDateTime.now().plusDays(30), "Salão de Festas",
                "Casamento", 100, "Preciso de buffet completo",
                StatusPedido.PENDENTE, null, null, null, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("criarPedido")
    class CriarPedido {

        @Test
        @DisplayName("Deve criar pedido com sucesso e enviar email ao prestador")
        void deveCriarPedidoComSucesso() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findById(prestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoMapper.toEntity(requestDTO, cliente, prestador)).thenReturn(pedido);
            when(pedidoRepository.save(pedido)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

            var resultado = service.criarPedido(requestDTO, usuarioCliente);

            assertThat(resultado).isNotNull();
            assertThat(resultado.status()).isEqualTo(StatusPedido.PENDENTE);
            verify(pedidoRepository).save(pedido);
            verify(emailService).enviarEmail(eq("prestador@test.com"), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarPedido(requestDTO, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CLIENTE_NAO_ENCONTRADO));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findById(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarPedido(requestDTO, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarPedidosPendentes")
    class ListarPedidosPendentes {

        @Test
        @DisplayName("Deve listar pedidos pendentes do prestador")
        void deveListarPedidosPendentes() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findByPrestadorIdAndStatusPedido(prestadorId, StatusPedido.PENDENTE))
                    .thenReturn(List.of(pedido));
            when(pedidoMapper.toResponseList(List.of(pedido))).thenReturn(List.of(responseDTO));

            var resultado = service.listarPedidosPendentes(usuarioPrestador);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).status()).isEqualTo(StatusPedido.PENDENTE);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarPedidosPendentes(usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("listarPedidosDoCliente")
    class ListarPedidosDoCliente {

        @Test
        @DisplayName("Deve listar todos os pedidos do cliente em ordem decrescente")
        void deveListarPedidosDoCliente() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findByClienteIdOrderByDataHoraCriacaoDesc(clienteId))
                    .thenReturn(List.of(pedido));
            when(pedidoMapper.toResponseList(List.of(pedido))).thenReturn(List.of(responseDTO));

            var resultado = service.listarPedidosDoCliente(usuarioCliente);

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarPedidosDoCliente(usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CLIENTE_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("listarTodosPedidosDoPrestador")
    class ListarTodosPedidosDoPrestador {

        @Test
        @DisplayName("Deve listar todos os pedidos do prestador em ordem decrescente")
        void deveListarTodosPedidosDoPrestador() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findByPrestadorIdOrderByDataHoraCriacaoDesc(prestadorId))
                    .thenReturn(List.of(pedido));
            when(pedidoMapper.toResponseList(List.of(pedido))).thenReturn(List.of(responseDTO));

            var resultado = service.listarTodosPedidosDoPrestador(usuarioPrestador);

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.listarTodosPedidosDoPrestador(usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("enviarOrcamento")
    class EnviarOrcamento {

        @Test
        @DisplayName("Deve enviar orçamento, mudar status para ORCADO e notificar cliente")
        void deveEnviarOrcamentoComSucesso() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.save(pedido)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

            service.enviarOrcamento(pedidoId, orcamentoDTO, usuarioPrestador);

            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ORCADO);
            verify(pedidoMapper).updatePedidoFromOrcamento(orcamentoDTO, pedido);
            verify(pedidoRepository).save(pedido);
            verify(emailService).enviarEmail(eq("cliente@test.com"), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, orcamentoDTO, usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado")
        void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, orcamentoDTO, usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_NAO_ENCONTRADO));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não pertence ao prestador")
        void deveLancarExcecaoQuandoPedidoNaoPertenceAoPrestador() {
            var outroPrestador = new Prestador();
            outroPrestador.setId(UUID.randomUUID());
            pedido.setPrestador(outroPrestador);

            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.enviarOrcamento(pedidoId, orcamentoDTO, usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_SEM_PERMISSAO));

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("recusarPedido")
    class RecusarPedido {

        @Test
        @DisplayName("Deve recusar pedido, mudar status para RECUSADO e notificar cliente")
        void deveRecusarPedidoComSucesso() {
            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            service.recusarPedido(pedidoId, usuarioPrestador);

            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.RECUSADO);
            verify(pedidoRepository).save(pedido);
            verify(emailService).enviarEmail(eq("cliente@test.com"), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não pertence ao prestador")
        void deveLancarExcecaoQuandoPedidoNaoPertenceAoPrestador() {
            var outroPrestador = new Prestador();
            outroPrestador.setId(UUID.randomUUID());
            pedido.setPrestador(outroPrestador);

            when(prestadorRepository.findByUsuarioId(usuarioPrestadorId)).thenReturn(Optional.of(prestador));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.recusarPedido(pedidoId, usuarioPrestador))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_SEM_PERMISSAO));

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("aceitarOrcamento")
    class AceitarOrcamento {

        @Test
        @DisplayName("Deve aceitar orçamento com sucesso e notificar prestador")
        void deveAceitarOrcamentoComSucesso() {
            pedido.setStatusPedido(StatusPedido.ORCADO);

            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.existsByPrestadorIdAndDataEventoAndStatusPedido(
                    prestadorId, pedido.getDataEvento(), StatusPedido.ACEITO)).thenReturn(false);
            when(pedidoRepository.save(pedido)).thenReturn(pedido);
            when(pedidoMapper.toResponseDTO(pedido)).thenReturn(responseDTO);

            var resultado = service.aceitarOrcamento(pedidoId, usuarioCliente);

            assertThat(resultado).isNotNull();
            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ACEITO);
            verify(emailService).enviarEmail(eq("prestador@test.com"), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status do pedido não é ORCADO")
        void deveLancarExcecaoQuandoStatusNaoEOrcado() {
            pedido.setStatusPedido(StatusPedido.PENDENTE);

            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_STATUS_INVALIDO));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador já tem compromisso na mesma data")
        void deveLancarExcecaoQuandoHaConflitoDeAgenda() {
            pedido.setStatusPedido(StatusPedido.ORCADO);

            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
            when(pedidoRepository.existsByPrestadorIdAndDataEventoAndStatusPedido(
                    prestadorId, pedido.getDataEvento(), StatusPedido.ACEITO)).thenReturn(true);

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_CONFLITO_AGENDA));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não pertence ao cliente")
        void deveLancarExcecaoQuandoPedidoNaoPertenceAoCliente() {
            pedido.setStatusPedido(StatusPedido.ORCADO);
            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());
            pedido.setCliente(outroCliente);

            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.aceitarOrcamento(pedidoId, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_SEM_PERMISSAO));

            verify(pedidoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancelarPedidoPeloCliente")
    class CancelarPedidoPeloCliente {

        @Test
        @DisplayName("Deve cancelar pedido, mudar status para CANCELADO e notificar prestador")
        void deveCancelarPedidoComSucesso() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente);

            assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
            verify(pedidoRepository).save(pedido);
            verify(emailService).enviarEmail(eq("prestador@test.com"), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não encontrado ao cancelar")
        void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_NAO_ENCONTRADO));

            verify(pedidoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pedido não pertence ao cliente ao cancelar")
        void deveLancarExcecaoQuandoPedidoNaoPertenceAoCliente() {
            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());
            pedido.setCliente(outroCliente);

            when(clienteRepository.findByUsuarioId(usuarioClienteId)).thenReturn(Optional.of(cliente));
            when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedidoId, usuarioCliente))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PEDIDO_SEM_PERMISSAO));

            verify(pedidoRepository, never()).save(any());
        }
    }
}
