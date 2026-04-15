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
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository, PrestadorRepository prestadorRepository, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Transactional
    public Pedido criarPedido(PedidoRequestDTO dto, Usuario usuarioLogado) {

        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o usuário logado."));

        Prestador prestador = prestadorRepository.findById(dto.prestadorId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado."));

        Pedido pedido = pedidoMapper.toEntity(dto, cliente, prestador);

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosPendentes(Usuario usuarioLogado) {

        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        List<Pedido> pedidos = pedidoRepository.findByPrestadorIdAndStatusPedido(prestador.getId(), StatusPedido.PENDENTE);
        return pedidoMapper.toResponseList(pedidos);
    }

    @Transactional
    public PedidoResponseDTO enviarOrcamento(UUID pedidoId, OrcamentoRequestDTO dto, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoEPermanecerSeguro(pedidoId, usuarioLogado);

        // Aplicamos o MapStruct para atualizar os campos de orçamento
        pedidoMapper.updatePedidoFromOrcamento(dto, pedido);
        pedido.setStatusPedido(StatusPedido.ORCADO);

        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public void recusarPedido(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoEPermanecerSeguro(pedidoId, usuarioLogado);
        pedido.setStatusPedido(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);
    }

    private Pedido buscarPedidoEPermanecerSeguro(UUID pedidoId, Usuario usuarioLogado) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

        if (!pedido.getPrestador().getId().equals(prestador.getId())) {
            throw new RuntimeException("Você não tem permissão para alterar este pedido.");
        }
        return pedido;
    }

    @Transactional
    public PedidoResponseDTO aceitarOrcamento(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoClienteSeguro(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.ORCADO) {
            throw new RuntimeException("Apenas pedidos orçados podem ser aceitos.");
        }

        pedido.setStatusPedido(StatusPedido.ACEITO);
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public void cancelarPedidoPeloCliente(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoClienteSeguro(pedidoId, usuarioLogado);

        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    // Método auxiliar de segurança para o Cliente
    private Pedido buscarPedidoClienteSeguro(UUID pedidoId, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new RuntimeException("Você não tem permissão para acessar este pedido.");
        }
        return pedido;
    }
}