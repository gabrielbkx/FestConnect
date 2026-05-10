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
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.email.EmailTemplates;
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
    private final EmailService emailService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         PrestadorRepository prestadorRepository,
                         PedidoMapper pedidoMapper,
                         EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.pedidoMapper = pedidoMapper;
        this.emailService = emailService;
    }

    @Transactional
    public Pedido criarPedido(PedidoRequestDTO dto, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado para o usuário logado."));

        Prestador prestador = prestadorRepository.findById(dto.prestadorId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado."));

        Pedido pedido = pedidoMapper.toEntity(dto, cliente, prestador);
        Pedido salvo = pedidoRepository.save(pedido);

        emailService.enviarEmail(
                prestador.getUsuario().getEmail(),
                "FestConnect - Novo pedido de orçamento",
                EmailTemplates.novoPedidoParaPrestador(salvo)
        );

        return salvo;
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

        pedidoMapper.updatePedidoFromOrcamento(dto, pedido);
        pedido.setStatusPedido(StatusPedido.ORCADO);
        PedidoResponseDTO resposta = pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));

        emailService.enviarEmail(
                pedido.getCliente().getUsuario().getEmail(),
                "FestConnect - Seu orçamento está pronto",
                EmailTemplates.orcamentoEnviadoParaCliente(pedido)
        );

        return resposta;
    }

    @Transactional
    public void recusarPedido(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoEPermanecerSeguro(pedidoId, usuarioLogado);
        pedido.setStatusPedido(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);

        emailService.enviarEmail(
                pedido.getCliente().getUsuario().getEmail(),
                "FestConnect - Prestador não disponível",
                EmailTemplates.pedidoRecusadoParaCliente(pedido)
        );
    }

    @Transactional
    public PedidoResponseDTO aceitarOrcamento(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoClienteSeguro(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.ORCADO) {
            throw new RuntimeException("Apenas pedidos orcados podem ser aceitos.");
        }

        boolean conflito = pedidoRepository.existsByPrestadorIdAndDataEventoAndStatusPedido(
                pedido.getPrestador().getId(), pedido.getDataEvento(), StatusPedido.ACEITO);
        if (conflito) {
            throw new RuntimeException("O prestador ja possui um pedido confirmado nesta data.");
        }

        pedido.setStatusPedido(StatusPedido.ACEITO);
        PedidoResponseDTO resposta = pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));

        emailService.enviarEmail(
                pedido.getPrestador().getUsuario().getEmail(),
                "FestConnect - Orçamento aceito!",
                EmailTemplates.orcamentoAceitoParaPrestador(pedido)
        );

        return resposta;
    }

    @Transactional
    public void cancelarPedidoPeloCliente(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoClienteSeguro(pedidoId, usuarioLogado);
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);

        emailService.enviarEmail(
                pedido.getPrestador().getUsuario().getEmail(),
                "FestConnect - Pedido cancelado",
                EmailTemplates.pedidoCanceladoParaPrestador(pedido)
        );
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
