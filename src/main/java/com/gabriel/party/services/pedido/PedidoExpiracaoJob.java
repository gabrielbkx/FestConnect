package com.gabriel.party.services.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.email.EmailTemplates;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class PedidoExpiracaoJob {

    private static final Logger logger = Logger.getLogger(PedidoExpiracaoJob.class.getName());

    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;

    public PedidoExpiracaoJob(PedidoRepository pedidoRepository, EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void expirarPedidosVencidos() {
        var agora = LocalDateTime.now();
        expirarPendentesSemResposta(agora);
        expirarOrcamentosNaoAceitos(agora);
        expirarEntradasNaoPagas(agora);
    }

    private void expirarPendentesSemResposta(LocalDateTime agora) {
        var aExpirar = pedidoRepository.findByStatusPedidoAndPrazoRespostaBefore(StatusPedido.PENDENTE, agora);
        if (aExpirar.isEmpty()) return;

        aExpirar.forEach(pedido -> {
            pedido.setStatusPedido(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            emailService.enviarAposCommit(
                    pedido.getCliente().getUsuario().getEmail(),
                    "Resenha - Pedido encerrado",
                    EmailTemplates.prestadorNaoRespondeuParaCliente(pedido)
            );
            emailService.enviarAposCommit(
                    pedido.getPrestador().getUsuario().getEmail(),
                    "Resenha - Prazo de resposta expirado",
                    EmailTemplates.prazoPerdidoParaPrestador(pedido)
            );
        });


        logger.info("[PedidoExpiracaoJob] " + aExpirar.size() + " pedido(s) PENDENTE expirado(s) por falta de resposta.");
    }

    private void expirarOrcamentosNaoAceitos(LocalDateTime agora) {
        var aExpirar = pedidoRepository.findByStatusPedidoAndValidadeOrcamentoBefore(StatusPedido.ORCADO, agora);
        if (aExpirar.isEmpty()) return;

        aExpirar.forEach(pedido -> {
            pedido.setStatusPedido(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            emailService.enviarAposCommit(
                    pedido.getCliente().getUsuario().getEmail(),
                    "Resenha - Orçamento expirado",
                    EmailTemplates.orcamentoExpiradoParaCliente(pedido)
            );

            emailService.enviarAposCommit(
                    pedido.getPrestador().getUsuario().getEmail(),
                    "Resenha - Reserva liberada",
                    EmailTemplates.reservaLiberadaParaPrestador(pedido)
            );
        });

        logger.info("[PedidoExpiracaoJob] " + aExpirar.size() + " orcamento(s) ORCADO expirado(s) por falta de aceite.");
    }

    private void expirarEntradasNaoPagas(LocalDateTime agora) {
        var aExpirar = pedidoRepository.findByStatusPedidoAndPrazoEntradaBefore(StatusPedido.AGUARDANDO_ENTRADA, agora);
        if (aExpirar.isEmpty()) return;

        aExpirar.forEach(pedido -> {
            pedido.setStatusPedido(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            emailService.enviarAposCommit(
                    pedido.getCliente().getUsuario().getEmail(),
                    "Resenha - Reserva não efetivada",
                    EmailTemplates.entradaNaoPagaParaCliente(pedido)
            );
        });

        logger.info("[PedidoExpiracaoJob] " + aExpirar.size() + " pedido(s) AGUARDANDO_ENTRADA expirado(s) por falta de pagamento do sinal.");
    }
}
