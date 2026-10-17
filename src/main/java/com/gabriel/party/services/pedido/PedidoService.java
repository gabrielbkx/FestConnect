package com.gabriel.party.services.pedido;

import com.gabriel.party.dtos.pedido.OrcamentoLoteItemDTO;
import com.gabriel.party.dtos.pedido.OrcamentoLoteRequestDTO;
import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.IntervaloOcupadoDTO;
import com.gabriel.party.model.agenda.BloqueioAgenda;
import com.gabriel.party.repositories.agenda.BloqueioAgendaRepository;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.pedido.PedidoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.evento.Evento;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.evento.EventoRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.email.EmailTemplates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PedidoService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM");

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PrestadorRepository prestadorRepository;
    private final ItemCatalogoRepository itemCatalogoRepository;
    private final EventoRepository eventoRepository;
    private final BloqueioAgendaRepository bloqueioRepository;
    private final PedidoMapper pedidoMapper;
    private final EmailService emailService;
    private final long prazoRespostaHoras;
    private final long prazoEntradaHoras;

    public PedidoService(PedidoRepository pedidoRepository,
                         ClienteRepository clienteRepository,
                         PrestadorRepository prestadorRepository,
                         ItemCatalogoRepository itemCatalogoRepository,
                         EventoRepository eventoRepository,
                         BloqueioAgendaRepository bloqueioRepository,
                         PedidoMapper pedidoMapper,
                         EmailService emailService,
                         @Value("${festconnect.pedido.prazo-resposta-horas}") Long prazoRespostaHoras,
                         @Value("${festconnect.pedido.prazo-entrada-horas}") Long prazoEntradaHoras) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.prestadorRepository = prestadorRepository;
        this.itemCatalogoRepository = itemCatalogoRepository;
        this.eventoRepository = eventoRepository;
        this.bloqueioRepository = bloqueioRepository;
        this.pedidoMapper = pedidoMapper;
        this.emailService = emailService;
        this.prazoRespostaHoras = prazoRespostaHoras;
        this.prazoEntradaHoras = prazoEntradaHoras;
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

        Evento evento = null;
        if (dto.eventoId() != null) {
            evento = eventoRepository.findById(dto.eventoId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENTO_NAO_ENCONTRADO, dto.eventoId().toString()));
            if (!evento.getCliente().getId().equals(cliente.getId())) {
                throw new AppException(ErrorCode.EVENTO_SEM_PERMISSAO);
            }
        }

        Pedido pedido = pedidoMapper.toEntity(dto, cliente, prestador, item, evento);
        pedido.setPrazoResposta(calcularPrazo(pedido.getDataEvento()));
        pedido.setDataFim(pedido.getDataEvento().plusMinutes(item.getDuracaoMinutos()));
        recusarSeAgendaCheia(prestador.getId(), pedido.getDataEvento(), pedido.getDataFim(), false);
        Pedido salvo = pedidoRepository.save(pedido);

        emailService.enviarEmail(
                prestador.getUsuario().getEmail(),
                "Resenha - Novo pedido de orçamento",
                EmailTemplates.novoPedidoParaPrestador(salvo)
        );

        return pedidoMapper.toResponseDTO(salvo);
    }

    /**
     * Recusa (409) se a agenda do prestador está CHEIA no intervalo [inicio, fim). "Cheia" = o número
     * de eventos RESERVADO que se sobrepõem já alcançou a capacidade (eventos simultâneos). A folga é
     * aplicada expandindo a janela para [inicio - folga, fim + folga).
     * <p>
     * {@code comLock} trava a linha do prestador (SELECT ... FOR UPDATE) para serializar reservas
     * concorrentes: com capacidade > 1 não dá para usar a restrição de exclusão do banco (ela só
     * garante zero sobreposição), então o lock é o que mantém a contagem consistente. Será usado no
     * pagamento do sinal (#99, fatia 4), que é o momento em que a data vira RESERVADO. Na
     * criação/orçamento/aceite (que não geram RESERVADO) é uma pré-checagem leve, sem lock.
     */
    private void recusarSeAgendaCheia(UUID prestadorId, LocalDateTime inicio, LocalDateTime fim, boolean comLock) {
        Prestador prestador = (comLock
                ? prestadorRepository.buscarComLock(prestadorId)
                : prestadorRepository.findById(prestadorId))
                .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

        LocalDateTime de = inicio.minusMinutes(prestador.getFolgaMinutos());
        LocalDateTime ate = fim.plusMinutes(prestador.getFolgaMinutos());
        List<Pedido> conflitos = pedidoRepository.buscarSobreposicoes(prestadorId, de, ate, StatusPedido.RESERVADO);
        List<BloqueioAgenda> bloqueios = bloqueioRepository.buscarSobreposicoes(prestadorId, de, ate);

        // Pedido confirmado e bloqueio ocupam a agenda do mesmo jeito: somam na conta da capacidade.
        if (conflitos.size() + bloqueios.size() >= prestador.getEventosSimultaneos()) {
            if (!conflitos.isEmpty()) {
                Pedido jaConfirmado = conflitos.get(0);
                throw new AppException(ErrorCode.PEDIDO_CONFLITO_AGENDA,
                        jaConfirmado.getDataEvento().format(HORA),
                        jaConfirmado.getDataFim().format(HORA),
                        jaConfirmado.getDataEvento().format(DIA));
            }
            BloqueioAgenda bloqueio = bloqueios.get(0);
            throw new AppException(ErrorCode.PEDIDO_CONFLITO_AGENDA,
                    bloqueio.getInicio().format(HORA),
                    bloqueio.getFim().format(HORA),
                    bloqueio.getInicio().format(DIA));
        }
    }

    /** Agenda pública do prestador: só os intervalos ocupados (RESERVADO) na janela, sem dado de cliente. */
    @Transactional(readOnly = true)
    public List<IntervaloOcupadoDTO> agendaOcupada(UUID prestadorId, LocalDateTime de, LocalDateTime ate) {
        // A agenda ocupada junta eventos confirmados e bloqueios manuais, ordenados por início.
        return Stream.concat(
                        pedidoRepository.agendaOcupada(prestadorId, de, ate).stream(),
                        bloqueioRepository.agendaOcupada(prestadorId, de, ate).stream())
                .sorted(java.util.Comparator.comparing(IntervaloOcupadoDTO::inicio))
                .toList();
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

        return pedidoRepository.findByPrestadorIdOrderByDataHoraCriacaoDesc(prestador.getId()).stream()
                .map(pedido -> comContatoDoCliente(pedido, true))
                .toList();
    }

    /**
     * Depois do aceite, o prestador precisa falar com o cliente. Revela o WhatsApp do cliente só
     * nesse caso (status RESERVADO e quem lê é o prestador do pedido); nos demais, o campo fica nulo.
     */
    private PedidoResponseDTO comContatoDoCliente(Pedido pedido, boolean solicitanteEhPrestador) {
        PedidoResponseDTO dto = pedidoMapper.toResponseDTO(pedido);
        if (solicitanteEhPrestador && pedido.getStatusPedido() == StatusPedido.RESERVADO) {
            return pedidoMapper.comWhatsappCliente(dto, pedido.getCliente().getWhatsapp());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new AppException(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoId.toString()));

        boolean ehClienteDono = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .map(c -> c.getId().equals(pedido.getCliente().getId()))
                .orElse(false);
        boolean ehPrestadorDono = prestadorRepository.findByUsuarioId(usuarioLogado.getId())
                .map(p -> p.getId().equals(pedido.getPrestador().getId()))
                .orElse(false);

        if (!ehClienteDono && !ehPrestadorDono) {
            throw new AppException(ErrorCode.PEDIDO_SEM_PERMISSAO);
        }

        return comContatoDoCliente(pedido, ehPrestadorDono);
    }

    @Transactional
    public PedidoResponseDTO enviarOrcamento(UUID pedidoId, OrcamentoRequestDTO dto, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoPrestador(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.PENDENTE) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }
        if (pedido.getPrazoResposta() != null && pedido.getPrazoResposta().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.PEDIDO_PRAZO_EXPIRADO);
        }

        // Não deixa orçar um horário que já está confirmado para outro cliente.
        recusarSeAgendaCheia(pedido.getPrestador().getId(), pedido.getDataEvento(), pedido.getDataFim(), false);
        // Outros orçados disputando o mesmo horário (este ainda é PENDENTE, então não se conta).
        int concorrentes = pedidoRepository.buscarSobreposicoes(
                pedido.getPrestador().getId(), pedido.getDataEvento(), pedido.getDataFim(), StatusPedido.ORCADO).size();

        pedidoMapper.updatePedidoFromOrcamento(dto, pedido);
        pedido.setValidadeOrcamento(calcularPrazo(pedido.getDataEvento()));
        pedido.setStatusPedido(StatusPedido.ORCADO);
        PedidoResponseDTO resposta = pedidoMapper.comPedidosConcorrentes(
                pedidoMapper.toResponseDTO(pedidoRepository.save(pedido)), concorrentes);

        emailService.enviarEmail(
                pedido.getCliente().getUsuario().getEmail(),
                "Resenha - Seu orçamento está pronto",
                EmailTemplates.orcamentoEnviadoParaCliente(pedido)
        );

        return resposta;
    }

    @Transactional
    public void recusarPedido(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoPrestador(pedidoId, usuarioLogado);

        if (!pedido.getStatusPedido().podeIrPara(StatusPedido.RECUSADO)) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }

        pedido.setStatusPedido(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);

        emailService.enviarEmail(
                pedido.getCliente().getUsuario().getEmail(),
                "Resenha - Prestador não disponível",
                EmailTemplates.pedidoRecusadoParaCliente(pedido)
        );
    }

    /**
     * O cliente aceita o orçamento. Aqui a data ainda NÃO é travada: o aceite só calcula o sinal
     * (arras) e abre o prazo para pagá-lo. Quem garante a data é o pagamento do sinal (#99, fatia 4),
     * que leva o pedido a RESERVADO. Por isso vários clientes podem aceitar o mesmo horário; a disputa
     * é resolvida no pagamento, sob lock. O status vai para AGUARDANDO_ENTRADA.
     */
    @Transactional
    public PedidoResponseDTO aceitarOrcamento(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoCliente(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.ORCADO) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }

        pedido.setValorSinal(calcularSinal(pedido.getValor(), pedido.getPrestador().getPercentualSinal()));
        pedido.setPrazoEntrada(LocalDateTime.now().plusHours(prazoEntradaHoras));
        pedido.setStatusPedido(StatusPedido.AGUARDANDO_ENTRADA);
        PedidoResponseDTO resposta = pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));

        emailService.enviarEmail(
                pedido.getCliente().getUsuario().getEmail(),
                "Resenha - Pague o sinal para reservar a data",
                EmailTemplates.sinalPendenteParaCliente(pedido)
        );

        return resposta;
    }

    /** Sinal = valor × percentual do prestador, arredondado a 2 casas. Nulo se ainda não há valor. */
    private java.math.BigDecimal calcularSinal(java.math.BigDecimal valor, Integer percentual) {
        if (valor == null || percentual == null) {
            return null;
        }
        return valor
                .multiply(java.math.BigDecimal.valueOf(percentual))
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }

    @Transactional
    public void recusarOrcamentoPeloCliente(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoCliente(pedidoId, usuarioLogado);

        if (pedido.getStatusPedido() != StatusPedido.ORCADO) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }

        pedido.setStatusPedido(StatusPedido.RECUSADO);
        pedidoRepository.save(pedido);

        emailService.enviarAposCommit(
                pedido.getPrestador().getUsuario().getEmail(),
                "Resenha - Orçamento recusado",
                EmailTemplates.orcamentoRecusadoPeloClienteParaPrestador(pedido)
        );
    }

    @Transactional
    public void cancelarPedidoPeloCliente(UUID pedidoId, Usuario usuarioLogado) {
        Pedido pedido = buscarPedidoComPermissaoCliente(pedidoId, usuarioLogado);

        if (!pedido.getStatusPedido().podeIrPara(StatusPedido.CANCELADO)) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }

        pedido.setStatusPedido(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);

        emailService.enviarEmail(
                pedido.getPrestador().getUsuario().getEmail(),
                "Resenha - Pedido cancelado",
                EmailTemplates.pedidoCanceladoParaPrestador(pedido)
        );
    }

    @Transactional
    public List<PedidoResponseDTO> criarPedidosEmLote(UUID eventoId, OrcamentoLoteRequestDTO dto, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENTO_NAO_ENCONTRADO, eventoId.toString()));

        if (!evento.getCliente().getId().equals(cliente.getId())) {
            throw new AppException(ErrorCode.EVENTO_SEM_PERMISSAO);
        }

        List<StatusPedido> statusAtivos = List.of(StatusPedido.PENDENTE, StatusPedido.ORCADO);

        // carrega e valida cada item antes de salvar qualquer um
        record ItemResolvido(Prestador prestador, com.gabriel.party.model.itemcatalogo.ItemCatalogo item) {}
        List<ItemResolvido> resolvidos = new ArrayList<>();

        for (OrcamentoLoteItemDTO itemDTO : dto.itens()) {
            Prestador prestador = prestadorRepository.findById(itemDTO.prestadorId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRESTADOR_NAO_ENCONTRADO, itemDTO.prestadorId().toString()));

            com.gabriel.party.model.itemcatalogo.ItemCatalogo item = itemCatalogoRepository.findByIdAndAtivoTrue(itemDTO.itemCatalogoId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO, itemDTO.itemCatalogoId().toString()));

            if (!item.getPrestador().getId().equals(prestador.getId())) {
                throw new AppException(ErrorCode.REGRA_NEGOCIO_VIOLADA, "O item solicitado não pertence ao prestador informado.");
            }

            if (pedidoRepository.existsByPrestadorIdAndItemCatalogoIdAndEventoIdAndStatusPedidoIn(
                    prestador.getId(), item.getId(), eventoId, statusAtivos)) {
                throw new AppException(ErrorCode.PEDIDO_LOTE_DUPLICADO, prestador.getNomeCompleto());
            }

            // Não oferece no lote um prestador já confirmado no horário do evento
            LocalDateTime inicio = evento.getDataEvento();
            recusarSeAgendaCheia(prestador.getId(), inicio, inicio.plusMinutes(item.getDuracaoMinutos()), false);

            resolvidos.add(new ItemResolvido(prestador, item));
        }

        // valida max 3 por categoria
        Map<String, Long> porCategoria = resolvidos.stream()
                .collect(Collectors.groupingBy(r -> r.item().getCategoria().getNome(), Collectors.counting()));

        porCategoria.forEach((categoria, qtd) -> {
            if (qtd > 3) {
                throw new AppException(ErrorCode.PEDIDO_LOTE_LIMITE_CATEGORIA, categoria);
            }
        });

        LocalDateTime prazo = calcularPrazo(evento.getDataEvento());

        List<Pedido> pedidosSalvos = new ArrayList<>();
        for (ItemResolvido r : resolvidos) {
            Pedido pedido = pedidoMapper.toEntityFromEvento(cliente, r.prestador(), r.item(), evento);
            pedido.setPrazoResposta(prazo);
            pedido.setDataFim(pedido.getDataEvento().plusMinutes(r.item().getDuracaoMinutos()));
            pedidosSalvos.add(pedidoRepository.save(pedido));
        }

        pedidosSalvos.forEach(p ->
                emailService.enviarAposCommit(
                        p.getPrestador().getUsuario().getEmail(),
                        "Resenha - Novo pedido de orçamento",
                        EmailTemplates.novoPedidoParaPrestador(p)
                )
        );

        return pedidoMapper.toResponseList(pedidosSalvos);
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

    private LocalDateTime calcularPrazo(LocalDateTime dataEvento) {
        LocalDateTime prazoPadrao = LocalDateTime.now().plusHours(prazoRespostaHoras);
        return prazoPadrao.isBefore(dataEvento) ? prazoPadrao : dataEvento;
    }

}
