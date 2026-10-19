package com.gabriel.party.services.pagamento;

import com.gabriel.party.dtos.pagamento.PagamentoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.pagamento.Pagamento;
import com.gabriel.party.model.pagamento.enums.StatusPagamento;
import com.gabriel.party.model.pagamento.enums.TipoPagamento;
import com.gabriel.party.model.pedido.Pedido;
import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.pagamento.PagamentoRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.pagamento.gateway.DadosCobranca;
import com.gabriel.party.services.pagamento.gateway.PagamentoGateway;
import com.gabriel.party.services.pagamento.gateway.ResultadoCobranca;
import com.gabriel.party.services.pedido.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orquestra o pagamento do sinal (#99). A plataforma não toca no dinheiro: abre a cobrança no
 * {@link PagamentoGateway}, registra o {@link Pagamento} e, se aprovado, pede ao {@link PedidoService}
 * para travar a data (RESERVADO). O gateway decide o desfecho; aqui só registramos e reagimos.
 */
@Service
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final PagamentoGateway gateway;
    private final PedidoService pedidoService;

    public PagamentoService(PagamentoRepository pagamentoRepository,
                            PedidoRepository pedidoRepository,
                            ClienteRepository clienteRepository,
                            PagamentoGateway gateway,
                            PedidoService pedidoService) {
        this.pagamentoRepository = pagamentoRepository;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.gateway = gateway;
        this.pedidoService = pedidoService;
    }

    @Transactional
    public PagamentoResponseDTO pagarSinal(UUID pedidoId, Usuario usuarioLogado) {
        Cliente cliente = clienteRepository.findByUsuarioId(usuarioLogado.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CLIENTE_NAO_ENCONTRADO, usuarioLogado.getId().toString()));

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new AppException(ErrorCode.PEDIDO_NAO_ENCONTRADO, pedidoId.toString()));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new AppException(ErrorCode.PEDIDO_SEM_PERMISSAO);
        }
        if (pedido.getStatusPedido() != StatusPedido.AGUARDANDO_ENTRADA) {
            throw new AppException(ErrorCode.PEDIDO_STATUS_INVALIDO, pedido.getStatusPedido().name());
        }
        if (pedido.getPrazoEntrada() != null && pedido.getPrazoEntrada().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.PEDIDO_PRAZO_EXPIRADO);
        }

        ResultadoCobranca resultado = gateway.cobrar(
                new DadosCobranca(pedido.getValorSinal(), "Sinal do pedido " + pedidoId, pedidoId));

        Pagamento pagamento = new Pagamento();
        pagamento.setPedido(pedido);
        pagamento.setTipo(TipoPagamento.SINAL);
        pagamento.setValor(pedido.getValorSinal());
        pagamento.setStatus(resultado.status());
        pagamento.setGateway(gateway.nome());
        pagamento.setIdExterno(resultado.idExterno());
        pagamento.setUrlComprovante(resultado.urlComprovante());
        Pagamento salvo = pagamentoRepository.save(pagamento);

        // Com o sinal aprovado, a data é travada agora. Se a agenda encheu durante o pagamento, o
        // reservarAposSinalAprovado lança 409 e a transação inteira (inclusive este registro) volta atrás.
        if (resultado.status() == StatusPagamento.APROVADO) {
            pedidoService.reservarAposSinalAprovado(pedidoId);
        }

        return new PagamentoResponseDTO(
                salvo.getId(), salvo.getTipo(), salvo.getValor(), salvo.getStatus(), salvo.getUrlComprovante(),
                pedido.getId(), pedido.getStatusPedido());
    }
}
