package com.gabriel.party.services.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoCreateDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.avaliacao.AvaliacaoMapper;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock private AvaliacaoRepository repository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private AvaliacaoMapper mapper;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PedidoRepository pedidoRepository;

    private AvaliacaoService service;

    private UUID usuarioId;
    private Cliente cliente;
    private Pedido pedido;
    private AvaliacaoCreateDTO dto;

    @BeforeEach
    void setUp() {
        service = new AvaliacaoService(repository, prestadorRepository, mapper, clienteRepository, pedidoRepository);

        usuarioId = UUID.randomUUID();
        cliente = new Cliente();
        cliente.setId(UUID.randomUUID());

        var prestador = new Prestador();
        prestador.setId(UUID.randomUUID());

        pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);

        dto = new AvaliacaoCreateDTO(5, "Ótimo", pedido.getId());

        lenient().when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
        lenient().when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
    }

    @Test
    @DisplayName("avaliar antes do evento (pedido RESERVADO) é recusado")
    void avaliar_antesDoEvento_barra() {
        pedido.setStatusPedido(StatusPedido.RESERVADO);

        assertThatThrownBy(() -> service.salvarAvaliacao(dto, usuarioId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AVALIACAO_SEM_CONTRATO_ACEITO);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"PENDENTE", "ORCADO", "AGUARDANDO_ENTRADA", "CANCELADO", "RECUSADO", "EXPIRADO"})
    @DisplayName("avaliar em qualquer status que não seja CONCLUIDO é recusado")
    void avaliar_statusNaoConcluido_barra(StatusPedido status) {
        pedido.setStatusPedido(status);

        assertThatThrownBy(() -> service.salvarAvaliacao(dto, usuarioId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AVALIACAO_SEM_CONTRATO_ACEITO);
    }

    @Test
    @DisplayName("avaliar depois do evento (pedido CONCLUIDO) é permitido")
    void avaliar_depoisDoEvento_ok() {
        pedido.setStatusPedido(StatusPedido.CONCLUIDO);
        when(repository.existsByPedidoId(pedido.getId())).thenReturn(false);
        when(mapper.toEntity(dto)).thenReturn(new Avaliacao());

        assertThatCode(() -> service.salvarAvaliacao(dto, usuarioId)).doesNotThrowAnyException();
        verify(repository).save(any(Avaliacao.class));
    }
}
