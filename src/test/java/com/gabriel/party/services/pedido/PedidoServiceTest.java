package com.gabriel.party.services.pedido;


import com.gabriel.party.dtos.pedido.OrcamentoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoRequestDTO;
import com.gabriel.party.dtos.pedido.PedidoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.pedido.PedidoMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private ItemCatalogoRepository itemCatalogoRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private com.gabriel.party.repositories.agenda.BloqueioAgendaRepository bloqueioRepository;
    @Mock private PedidoMapper pedidoMapper;
    @Mock private EmailService emailService;

    private PedidoService service;
    private Usuario usuarioCliente;
    private Cliente cliente;
    private Prestador prestador;
    private ItemCatalogo item;

    @BeforeEach
    void setUp() {
        // Injeção via construtor nos dá controle dos prazos (resposta e sinal), ambos 72h
        service = new PedidoService(pedidoRepository, clienteRepository, prestadorRepository,
                itemCatalogoRepository, eventoRepository, bloqueioRepository, pedidoMapper, emailService, 72L, 72L);

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
        item.setDuracaoMinutos(240);

        // O helper de agenda busca o prestador (capacidade/folga). Padrão: capacidade 1, folga 0.
        lenient().when(prestadorRepository.buscarComLock(prestador.getId())).thenReturn(Optional.of(prestador));
        lenient().when(prestadorRepository.findById(prestador.getId())).thenReturn(Optional.of(prestador));
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

    /** Pedido pronto para as ações de transição, com os campos que os templates de e-mail leem. */
    private Pedido pedidoCom(StatusPedido status) {
        var pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);
        pedido.setItemCatalogo(item);
        pedido.setStatusPedido(status);
        pedido.setTipoEvento("Casamento");
        pedido.setDataEvento(LocalDateTime.now().plusDays(10));
        return pedido;
    }

    private void mockarPrestadorDonoDoPedido(Pedido pedido) {
        when(prestadorRepository.findByUsuarioId(prestador.getUsuario().getId())).thenReturn(Optional.of(prestador));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
    }

    private void mockarClienteDonoDoPedido(Pedido pedido) {
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
    }

    // ── whatsappCliente: revelado só ao prestador do pedido e depois do aceite

    @Test
    @DisplayName("prestador vê o WhatsApp do cliente quando o pedido está RESERVADO")
    void buscarPorId_prestadorEPedidoAceito_revelaWhatsappDoCliente() {
        cliente.setWhatsapp("11912345678");
        var pedido = pedidoCom(StatusPedido.RESERVADO);
        var dtoBase = mock(PedidoResponseDTO.class);
        var dtoComWhatsapp = mock(PedidoResponseDTO.class);
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(prestadorRepository.findByUsuarioId(prestador.getUsuario().getId())).thenReturn(Optional.of(prestador));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dtoBase);
        when(pedidoMapper.comWhatsappCliente(dtoBase, "11912345678")).thenReturn(dtoComWhatsapp);

        var resultado = service.buscarPedidoPorId(pedido.getId(), prestador.getUsuario());

        assertThat(resultado).isSameAs(dtoComWhatsapp);
    }

    @Test
    @DisplayName("antes do aceite, o WhatsApp do cliente não é revelado ao prestador")
    void buscarPorId_prestadorMasAindaOrcado_naoRevela() {
        cliente.setWhatsapp("11912345678");
        var pedido = pedidoCom(StatusPedido.ORCADO);
        var dtoBase = mock(PedidoResponseDTO.class);
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(prestadorRepository.findByUsuarioId(prestador.getUsuario().getId())).thenReturn(Optional.of(prestador));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dtoBase);

        var resultado = service.buscarPedidoPorId(pedido.getId(), prestador.getUsuario());

        assertThat(resultado).isSameAs(dtoBase);
        verify(pedidoMapper, never()).comWhatsappCliente(any(), any());
    }

    @Test
    @DisplayName("o cliente não recebe o próprio WhatsApp de volta (só o prestador vê)")
    void buscarPorId_clienteDono_naoRevela() {
        cliente.setWhatsapp("11912345678");
        var pedido = pedidoCom(StatusPedido.RESERVADO);
        var dtoBase = mock(PedidoResponseDTO.class);
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dtoBase);

        var resultado = service.buscarPedidoPorId(pedido.getId(), usuarioCliente);

        assertThat(resultado).isSameAs(dtoBase);
        verify(pedidoMapper, never()).comWhatsappCliente(any(), any());
    }

    // Capacidade (eventos simultâneos), folga e bloqueio no aceite saíram daqui: o aceite não trava
    // mais a agenda (#99). Essa checagem sob lock passa para o pagamento do sinal (fatia 4), onde a
    // data vira RESERVADO; lá esses cenários voltam a ser testados. O pré-check leve de
    // criar/orçar/lote continua cobrindo o conflito básico de agenda.

    // ── recusarPedido (prestador): só vale enquanto o pedido ainda pode ser recusado

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"PENDENTE", "ORCADO"})
    @DisplayName("prestador recusa um pedido que ainda pode ser recusado")
    void recusarPedido_statusValido_recusa(StatusPedido status) {
        var pedido = pedidoCom(status);
        mockarPrestadorDonoDoPedido(pedido);

        service.recusarPedido(pedido.getId(), prestador.getUsuario());

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.RECUSADO);
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"RESERVADO", "RECUSADO", "CANCELADO", "EXPIRADO"})
    @DisplayName("recusar um pedido já encerrado devolve PEDIDO_STATUS_INVALIDO")
    void recusarPedido_statusInvalido_barra(StatusPedido status) {
        var pedido = pedidoCom(status);
        mockarPrestadorDonoDoPedido(pedido);

        assertThatThrownBy(() -> service.recusarPedido(pedido.getId(), prestador.getUsuario()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_STATUS_INVALIDO);
        assertThat(pedido.getStatusPedido()).isEqualTo(status); // não mudou
    }

    // ── cancelarPedidoPeloCliente: só antes do aceite

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"PENDENTE", "ORCADO"})
    @DisplayName("cliente cancela um pedido ainda não aceito")
    void cancelarPedido_statusValido_cancela(StatusPedido status) {
        var pedido = pedidoCom(status);
        mockarClienteDonoDoPedido(pedido);

        service.cancelarPedidoPeloCliente(pedido.getId(), usuarioCliente);

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.CANCELADO);
    }

    @ParameterizedTest
    @EnumSource(value = StatusPedido.class, names = {"RESERVADO", "RECUSADO", "CANCELADO", "EXPIRADO"})
    @DisplayName("cancelar um pedido aceito ou já encerrado devolve PEDIDO_STATUS_INVALIDO")
    void cancelarPedido_statusInvalido_barra(StatusPedido status) {
        var pedido = pedidoCom(status);
        mockarClienteDonoDoPedido(pedido);

        assertThatThrownBy(() -> service.cancelarPedidoPeloCliente(pedido.getId(), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_STATUS_INVALIDO);
        assertThat(pedido.getStatusPedido()).isEqualTo(status); // não mudou
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

    // ── enviarOrcamento: recusa horário confirmado e avisa concorrentes

    private Pedido pedidoPendente(LocalDateTime inicio) {
        var pedido = pedidoOrcado(inicio);
        pedido.setStatusPedido(StatusPedido.PENDENTE);
        pedido.setPrazoResposta(LocalDateTime.now().plusHours(10));
        return pedido;
    }

    private OrcamentoRequestDTO orcamento() {
        return new OrcamentoRequestDTO(java.math.BigDecimal.valueOf(2500), "Inclui tudo");
    }

    @Test
    @DisplayName("orçar um horário já confirmado é recusado (409)")
    void enviarOrcamento_horarioConfirmado_barra() {
        var inicio = LocalDateTime.now().plusDays(10);
        var pedido = pedidoPendente(inicio);
        var jaConfirmado = pedidoOrcado(LocalDateTime.of(2026, 12, 12, 20, 0));
        jaConfirmado.setStatusPedido(StatusPedido.RESERVADO);
        when(prestadorRepository.findByUsuarioId(prestador.getUsuario().getId())).thenReturn(Optional.of(prestador));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.buscarSobreposicoes(eq(prestador.getId()), any(), any(), eq(StatusPedido.RESERVADO)))
                .thenReturn(List.of(jaConfirmado));

        assertThatThrownBy(() -> service.enviarOrcamento(pedido.getId(), orcamento(), prestador.getUsuario()))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_CONFLITO_AGENDA);
        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.PENDENTE); // não orçou
    }

    @Test
    @DisplayName("sem confirmado no horário, a resposta avisa quantos outros orçados concorrem")
    void enviarOrcamento_avisaConcorrentes() {
        var inicio = LocalDateTime.now().plusDays(10);
        var pedido = pedidoPendente(inicio);
        when(prestadorRepository.findByUsuarioId(prestador.getUsuario().getId())).thenReturn(Optional.of(prestador));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.buscarSobreposicoes(eq(prestador.getId()), any(), any(), eq(StatusPedido.RESERVADO)))
                .thenReturn(List.of());
        when(pedidoRepository.buscarSobreposicoes(eq(prestador.getId()), any(), any(), eq(StatusPedido.ORCADO)))
                .thenReturn(List.of(pedidoOrcado(inicio), pedidoOrcado(inicio))); // 2 concorrentes
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        var dtoBase = mock(PedidoResponseDTO.class);
        when(pedidoMapper.toResponseDTO(pedido)).thenReturn(dtoBase);

        service.enviarOrcamento(pedido.getId(), orcamento(), prestador.getUsuario());

        verify(pedidoMapper).comPedidosConcorrentes(dtoBase, 2);
        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.ORCADO);
    }

    // ── aceitarOrcamento: não trava a agenda; calcula o sinal e abre o prazo de pagamento (#99)

    private Pedido pedidoOrcado(LocalDateTime inicio) {
        var pedido = new Pedido();
        pedido.setId(UUID.randomUUID());
        pedido.setCliente(cliente);
        pedido.setPrestador(prestador);
        pedido.setItemCatalogo(item);
        pedido.setStatusPedido(StatusPedido.ORCADO);
        pedido.setTipoEvento("Casamento");
        pedido.setLocalEvento("Salão X");
        pedido.setNumeroConvidados(100);
        pedido.setValor(java.math.BigDecimal.valueOf(2500));
        pedido.setDataEvento(inicio);
        pedido.setDataFim(inicio.plusMinutes(240));
        return pedido;
    }

    @Test
    @DisplayName("aceitar leva a AGUARDANDO_ENTRADA e calcula o sinal pelo percentual do prestador")
    void aceitarOrcamento_geraAguardandoEntradaComSinal() {
        var inicio = LocalDateTime.now().plusDays(10);
        var pedido = pedidoOrcado(inicio); // valor 2500
        prestador.setPercentualSinal(30);
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.aceitarOrcamento(pedido.getId(), usuarioCliente);

        assertThat(pedido.getStatusPedido()).isEqualTo(StatusPedido.AGUARDANDO_ENTRADA);
        assertThat(pedido.getValorSinal()).isEqualByComparingTo("750.00"); // 2500 × 30%
        assertThat(pedido.getPrazoEntrada()).isNotNull();
    }

    @Test
    @DisplayName("aceitar não trava a agenda: não consulta sobreposição nem pega lock do prestador")
    void aceitarOrcamento_naoTravaAgenda() {
        var inicio = LocalDateTime.now().plusDays(10);
        var pedido = pedidoOrcado(inicio);
        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(pedidoRepository.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.aceitarOrcamento(pedido.getId(), usuarioCliente);

        verify(pedidoRepository, never()).buscarSobreposicoes(any(), any(), any(), any());
        verify(prestadorRepository, never()).buscarComLock(any());
    }

    @Test
    @DisplayName("criarPedido grava o fim do pedido = início + a duração do item")
    void criarPedido_gravaDataFim() {
        LocalDateTime dataEvento = LocalDateTime.now().plusDays(30);
        mockarFluxoCriacao(dataEvento);   // item com 240 min (4h)

        service.criarPedido(novoRequest(dataEvento), usuarioCliente);

        var captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        assertThat(captor.getValue().getDataFim()).isEqualTo(dataEvento.plusMinutes(240));
    }

    @Test
    @DisplayName("criarPedido recusa um horário já confirmado para o prestador (409)")
    void criarPedido_horarioConfirmado_barra() {
        LocalDateTime dataEvento = LocalDateTime.now().plusDays(10);
        var jaConfirmado = pedidoOrcado(LocalDateTime.of(2026, 12, 12, 20, 0));
        jaConfirmado.setStatusPedido(StatusPedido.RESERVADO);

        when(clienteRepository.findByUsuarioId(usuarioCliente.getId())).thenReturn(Optional.of(cliente));
        when(prestadorRepository.findById(prestador.getId())).thenReturn(Optional.of(prestador));
        when(itemCatalogoRepository.findByIdAndAtivoTrue(item.getId())).thenReturn(Optional.of(item));
        var pedidoEntity = new Pedido();
        pedidoEntity.setPrestador(prestador);
        pedidoEntity.setDataEvento(dataEvento);
        when(pedidoMapper.toEntity(any(), any(), any(), any(), any())).thenReturn(pedidoEntity);
        when(pedidoRepository.buscarSobreposicoes(eq(prestador.getId()), any(), any(), eq(StatusPedido.RESERVADO)))
                .thenReturn(List.of(jaConfirmado));

        assertThatThrownBy(() -> service.criarPedido(novoRequest(dataEvento), usuarioCliente))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PEDIDO_CONFLITO_AGENDA);
        verify(pedidoRepository, never()).save(any());
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