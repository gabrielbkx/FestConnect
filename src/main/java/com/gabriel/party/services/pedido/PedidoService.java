package com.gabriel.party.services.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.pedido.PedidoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
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
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final PedidoMapper pedidoMapper;
    private final EmailService emailService;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         PrestadorRepository prestadorRepository,
                         ItemCatalogoRepository itemCatalogoRepository,
                         PedidoMapper pedidoMapper,
                         EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.pedidoMapper = pedidoMapper;
        this.emailService = emailService;
    }

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));

        Prestador prestador = prestadorRepository.findById(dto.prestadorId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, dto.prestadorId().toString()));

        ItemCatalogo item = itemCatalogoRepository.findByIdAndAtivoTrue(dto.itemCatalogoId())
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, dto.itemCatalogoId().toString()));

        if (!item.getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.REGRA_NEGOCIO_VIOLADA,
                    "O item solicitado não pertence ao prestador informado.");
        }

        Pedido pedido = pedidoMapper.toEntity(dto, cliente, prestador, item);
        Pedido salvo = pedidoRepository.save(pedido);

        emailService.enviarEmail(
                prestador.getUsuario().getEmail(),
                "FestConnect - Novo pedido de orçamento",
                EmailTemplates.novoPedidoParaPrestador(salvo)
        );

        return pedidoMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosPendentes(Usuario usuarioLogado) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        List<Pedido> pedidos = pedidoRepository.findByPrestadorIdAndStatusPedido(prestador.getId(), StatusPedido.PENDENTE);
        return pedidoMapper.toResponseList(pedidos);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPedidosDoCliente(Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));

        return pedidoMapper.toResponseList(
                pedidoRepository.findByClienteIdOrderByDataHoraCriacaoDesc(cliente.getId()));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodosPedidosDoPrestador(Usuario usuarioLogado) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        return pedidoMapper.toResponseList(
                pedidoRepository.findByPrestadorIdOrderByDataHoraCriacaoDesc(prestador.getId()));
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new AppException(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoId.toString()));

        boolean autorizado = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .map(c -> c.getId().equals(pedido.getCliente().getId()))
                .orElseGet(() -> prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                        .map(p -> p.getId().equals(pedido.getPrestador().getId()))
                        .orElse(false));

        if (!autorizado) {
            throw new AppException(ErrorCode.PEDIDO_SEM_PERMISSAO);
        }

        return pedidoMapper.toResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO enviarOrcamento(UUID pedidoId, OrcamentoRequestDTO dto, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoPrestador(pedidoId, usuarioLogado);

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
        Pedido pedido = buscarPedidoComPermissaoPrestador(pedidoId, usuarioLogado);
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
        Pedido pedido = buscarPedidoComPermissaoCliente(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.ORCADO) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }

        if (pedidoRepository.existsByPrestadorIdAndDataEventoAndStatusPedido(
                pedido.getPrestador().getId(), pedido.getDataEvento(), StatusPedido.ACEITO)) {
            throw new AppException(ErrorCode.PEDIDO_CONFLITO_AGENDA);
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
        Pedido pedido = buscarPedidoComPermissaoCliente(pedidoId, usuarioLogado);
        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);

        emailService.enviarEmail(
                pedido.getPrestador().getUsuario().getEmail(),
                "FestConnect - Pedido cancelado",
                EmailTemplates.pedidoCanceladoParaPrestador(pedido)
        );
    }

    private Pedido buscarPedidoComPermissaoPrestador(UUID pedidoId, Usuario usuarioLogado) {
        Prestador prestador = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new AppException(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoId.toString()));

        if (!pedido.getPrestador().getId().equals(prestador.getId())) {
            throw new AppException(ErrorCode.PEDIDO_SEM_PERMISSAO);
        }
        return pedido;
    }

    private Pedido buscarPedidoComPermissaoCliente(UUID pedidoId, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new AppException(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoId.toString()));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AppException(ErrorCode.PEDIDO_SEM_PERMISSAO);
        }
        return pedido;
    }
}
