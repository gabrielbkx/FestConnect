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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

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
                "FestConnect - Novo pedido de orcamento",
                "Ola, " + prestador.getNomeCompleto() + "!\n\n" +
                "Voce recebeu um novo pedido de orcamento de " + cliente.getNomeCompleto() + ".\n\n" +
                "Evento: " + salvo.getTipoEvento() + "\n" +
                "Data: " + salvo.getDataEvento().format(FORMATO_DATA) + "\n" +
                "Local: " + salvo.getLocalEvento() + "\n" +
                "Convidados: " + salvo.getNumeroConvidados() + "\n\n" +
                "Acesse a plataforma para enviar seu orcamento."
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
                "FestConnect - Seu orcamento esta pronto",
                "Ola, " + pedido.getCliente().getNomeCompleto() + "!\n\n" +
                pedido.getPrestador().getNomeCompleto() + " enviou um orcamento para o seu evento.\n\n" +
                "Evento: " + pedido.getTipoEvento() + "\n" +
                "Data: " + pedido.getDataEvento().format(FORMATO_DATA) + "\n" +
                "Valor: R$ " + pedido.getValor() + "\n" +
                "Validade do orcamento: " + pedido.getValidadeOrcamento().format(FORMATO_DATA) + "\n\n" +
                "Acesse a plataforma para aceitar ou recusar o orcamento."
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
                "FestConnect - Pedido nao disponivel",
                "Ola, " + pedido.getCliente().getNomeCompleto() + ".\n\n" +
                "Infelizmente " + pedido.getPrestador().getNomeCompleto() +
                " nao esta disponivel para o seu evento.\n\n" +
                "Evento: " + pedido.getTipoEvento() + "\n" +
                "Data: " + pedido.getDataEvento().format(FORMATO_DATA) + "\n\n" +
                "Explore outros prestadores na plataforma."
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
                "FestConnect - Orcamento aceito",
                "Ola, " + pedido.getPrestador().getNomeCompleto() + "!\n\n" +
                pedido.getCliente().getNomeCompleto() + " aceitou seu orcamento.\n\n" +
                "Evento: " + pedido.getTipoEvento() + "\n" +
                "Data: " + pedido.getDataEvento().format(FORMATO_DATA) + "\n" +
                "Local: " + pedido.getLocalEvento() + "\n" +
                "Valor: R$ " + pedido.getValor() + "\n\n" +
                "Entre em contato pelo WhatsApp do cliente para finalizar os detalhes."
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
                "Ola, " + pedido.getPrestador().getNomeCompleto() + ".\n\n" +
                pedido.getCliente().getNomeCompleto() + " cancelou o pedido para o seguinte evento:\n\n" +
                "Evento: " + pedido.getTipoEvento() + "\n" +
                "Data: " + pedido.getDataEvento().format(FORMATO_DATA) + "\n\n" +
                "O pedido foi encerrado na plataforma."
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
