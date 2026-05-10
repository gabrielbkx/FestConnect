package com.gabriel.party.services.pedido;

import com.gabriel.party.model.pedido.enums.StatusPedido;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.services.email.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Component
public class PedidoScheduler {

    private static final Logger logger = Logger.getLogger(PedidoScheduler.class.getName());
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;

    public PedidoScheduler(PedidoRepository pedidoRepository, EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void expirarOrcamentosVencidos() {
        var agora = LocalDateTime.now();
        var aExpirar = pedidoRepository.findByStatusPedidoAndValidadeOrcamentoBefore(StatusPedido.ORCADO, agora);

        if (aExpirar.isEmpty()) return;

        aExpirar.forEach(pedido -> {
            pedido.setStatusPedido(StatusPedido.EXPIRADO);
            pedidoRepository.save(pedido);

            emailService.enviarEmail(
                    pedido.getCliente().getUsuario().getEmail(),
                    "FestConnect - Orcamento expirado",
                    "Ola, " + pedido.getCliente().getNomeCompleto() + ".\n\n" +
                    "O orcamento de " + pedido.getPrestador().getNomeCompleto() +
                    " para o seu evento expirou.\n\n" +
                    "Evento: " + pedido.getTipoEvento() + "\n" +
                    "Data do evento: " + pedido.getDataEvento().format(FORMATO_DATA) + "\n\n" +
                    "Voce pode solicitar um novo orcamento diretamente na plataforma."
            );
        });

        logger.info("[PedidoScheduler] " + aExpirar.size() + " orcamento(s) expirado(s) e notificados.");
    }
}
