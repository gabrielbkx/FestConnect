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
        concluirEventosPassados(agora);
        enviarLembretes(agora);
    }

    /**
     * Lembra cliente e parceiro do evento reservado: um aviso quando falta menos de uma semana e outro
     * na véspera. As flags no pedido garantem idempotência — cada lembrete sai uma vez só, ainda que o
     * job rode de hora em hora. As janelas não se sobrepõem: semana é [agora+1d, agora+7d], véspera é
     * [agora, agora+1d].
     */
    private void enviarLembretes(LocalDateTime agora) {
        var semana = pedidoRepository.buscarParaLembreteSemana(
                StatusPedido.RESERVADO, agora.plusDays(1), agora.plusDays(7));
        semana.forEach(pedido -> {
            pedido.setLembreteSemanaEnviado(true);
            pedidoRepository.save(pedido);
            emailService.enviarAposCommit(pedido.getCliente().getUsuario().getEmail(),
                    "Resenha - Seu evento está chegando", EmailTemplates.lembreteParaCliente(pedido, "na próxima semana"));
            emailService.enviarAposCommit(pedido.getPrestador().getUsuario().getEmail(),
                    "Resenha - Evento na próxima semana", EmailTemplates.lembreteParaPrestador(pedido, "na próxima semana"));
        });

        var vespera = pedidoRepository.buscarParaLembreteVespera(
                StatusPedido.RESERVADO, agora, agora.plusDays(1));
        vespera.forEach(pedido -> {
            pedido.setLembreteVesperaEnviado(true);
            pedidoRepository.save(pedido);
            emailService.enviarAposCommit(pedido.getCliente().getUsuario().getEmail(),
                    "Resenha - Seu evento é amanhã", EmailTemplates.lembreteParaCliente(pedido, "amanhã"));
            emailService.enviarAposCommit(pedido.getPrestador().getUsuario().getEmail(),
                    "Resenha - Evento amanhã", EmailTemplates.lembreteParaPrestador(pedido, "amanhã"));
        });

        int total = semana.size() + vespera.size();
        if (total > 0) {
            logger.info("[PedidoExpiracaoJob] " + semana.size() + " lembrete(s) de semana e " + vespera.size() + " de véspera enviado(s).");
        }
    }

    /**
     * Evento reservado cujo fim já passou vira CONCLUIDO. Idempotente: só pega RESERVADO, então rodar
     * de novo não reprocessa quem já concluiu. É o que libera a avaliação (só depois do evento) e faz
     * o "pedidos concluídos" do perfil contar eventos que de fato aconteceram (#90).
     */
    private void concluirEventosPassados(LocalDateTime agora) {
        var aConcluir = pedidoRepository.findByStatusPedidoAndDataFimBefore(StatusPedido.RESERVADO, agora);
        if (aConcluir.isEmpty()) return;

        aConcluir.forEach(pedido -> {
            pedido.setStatusPedido(StatusPedido.CONCLUIDO);
            pedidoRepository.save(pedido);
        });

        logger.info("[PedidoExpiracaoJob] " + aConcluir.size() + " evento(s) RESERVADO concluído(s) por já terem passado.");
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
