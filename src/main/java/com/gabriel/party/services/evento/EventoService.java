package com.gabriel.party.services.evento;

import com.gabriel.party.dtos.evento.EventoRequestDTO;
import com.gabriel.party.dtos.evento.EventoResponseDTO;
import com.gabriel.party.dtos.evento.EventoStatusUpdateDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.evento.EventoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.evento.enums.StatusEvento;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.evento.EventoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.email.EmailTemplates;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;
    private final EventoMapper eventoMapper;

    public EventoService(EventoRepository eventoRepository,
                         ClienteRepository clienteRepository,
                         PedidoRepository pedidoRepository,
                         EmailService emailService,
                         EventoMapper eventoMapper) {
        this.eventoRepository = eventoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
        this.emailService = emailService;
        this.eventoMapper = eventoMapper;
    }

    @Transactional
    public EventoResponseDTO criar(EventoRequestDTO dto, Usuario usuarioLogado) {
        Cliente cliente = buscarClienteDoUsuario(usuarioLogado);
        Evento evento = eventoMapper.toEntity(dto, cliente);
        return eventoMapper.toResponseDTO(eventoRepository.save(evento));
    }

    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarDoCliente(Usuario usuarioLogado, StatusEvento status) {
        Cliente cliente = buscarClienteDoUsuario(usuarioLogado);
        List<Evento> eventos = status == null
                ? eventoRepository.findByClienteIdOrderByDataEventoDesc(cliente.getId())
                : eventoRepository.findByClienteIdAndStatusOrderByDataEventoDesc(cliente.getId(), status);
        return eventoMapper.toResponseList(eventos);
    }

    @Transactional(readOnly = true)
    public EventoResponseDTO buscarPorId(UUID eventoId, Usuario usuarioLogado) {
        Evento evento = buscarEventoDoCliente(eventoId, usuarioLogado);
        return eventoMapper.toResponseDTO(evento);
    }

    @Transactional
    public EventoResponseDTO atualizar(UUID eventoId, EventoRequestDTO dto, Usuario usuarioLogado) {
        Evento evento = buscarEventoDoCliente(eventoId, usuarioLogado);
        eventoMapper.atualizarEventoDoDTO(dto, evento);
        return eventoMapper.toResponseDTO(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponseDTO atualizarStatus(UUID eventoId, EventoStatusUpdateDTO dto, Usuario usuarioLogado) {
        Evento evento = buscarEventoDoCliente(eventoId, usuarioLogado);
        evento.setStatus(dto.status());
        eventoRepository.save(evento);

        if (dto.status() == StatusEvento.CANCELADO) {
            List<Pedido> pedidosAtivos = pedidoRepository.findByEventoIdAndStatusPedidoIn(
                    eventoId, List.of(StatusPedido.PENDENTE, StatusPedido.ORCADO));
            pedidosAtivos.forEach(p -> {
                p.setStatusPedido(StatusPedido.CANCELADO);
                emailService.enviarAposCommit(
                        p.getPrestador().getUsuario().getEmail(),
                        "FestConnect - Evento cancelado",
                        EmailTemplates.eventoCanceladoParaPrestador(p)
                );
            });
        }

        return eventoMapper.toResponseDTO(evento);
    }

    @Transactional
    public void deletar(UUID eventoId, Usuario usuarioLogado) {
        Evento evento = buscarEventoDoCliente(eventoId, usuarioLogado);
        // Desvincula pedidos antes de deletar — pedidos continuam existindo, só perdem o agrupamento
        if (evento.getPedidos() != null) {
            for (Pedido pedido : evento.getPedidos()) {
                pedido.setEvento(null);
            }
        }
        eventoRepository.delete(evento);
    }

    /**
     * Carrega e valida posse do evento. Usado por SugestaoService e fluxo de Pedido.
     */
    @Transactional(readOnly = true)
    public Evento buscarEventoDoClienteOuErro(UUID eventoId, Usuario usuarioLogado) {
        return buscarEventoDoCliente(eventoId, usuarioLogado);
    }

    private Evento buscarEventoDoCliente(UUID eventoId, Usuario usuarioLogado) {
        Cliente cliente = buscarClienteDoUsuario(usuarioLogado);
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENTO_NAO_ENCONTRADO, eventoId.toString()));

        if (!evento.getCliente().getId().equals(cliente.getId())) {
            throw new AppException(ErrorCode.EVENTO_SEM_PERMISSAO);
        }
        return evento;
    }

    private Cliente buscarClienteDoUsuario(Usuario usuarioLogado) {
        return clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));
    }
}
