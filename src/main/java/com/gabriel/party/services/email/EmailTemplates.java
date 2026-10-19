package com.gabriel.party.services.email;

import com.gabriel.party.model.pedido.Pedido;

import java.time.format.DateTimeFormatter;

public class EmailTemplates {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public static String novoPedidoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Solicitado por", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento()) +
                linha("Convidados", String.valueOf(pedido.getNumeroConvidados())) +
                (pedido.getDescricao() != null ? linha("Observações", pedido.getDescricao()) : "");

        return construir(
                "Novo pedido de orçamento",
                "Olá, " + nomePrestador + "!",
                "Você recebeu um novo pedido de orçamento. Acesse a plataforma para analisar os detalhes e enviar sua proposta.",
                detalhes,
                ""
        );
    }

    public static String orcamentoEnviadoParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Valor", "R$ " + pedido.getValor()) +
                linha("Válido até", pedido.getValidadeOrcamento().format(FORMATO_DATA));

        String rodape = caixa("info",
                "Para aceitar ou recusar o orçamento, acesse a Resenha. " +
                "Este orçamento expira em <strong>" + pedido.getValidadeOrcamento().format(FORMATO_DATA) + "</strong>.");

        return construir(
                "Seu orçamento está pronto",
                "Olá, " + nomeCliente + "!",
                "<strong>" + nomePrestador + "</strong> enviou um orçamento para o seu evento. Confira os detalhes abaixo:",
                detalhes,
                rodape
        );
    }

    public static String pedidoRecusadoParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "Não desanime! Explore outros prestadores disponíveis na Resenha para o seu evento.");

        return construir(
                "Prestador não disponível",
                "Olá, " + nomeCliente + ".",
                "Infelizmente <strong>" + nomePrestador + "</strong> não está disponível para atender ao seu pedido.",
                detalhes,
                rodape
        );
    }

    public static String orcamentoAceitoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String whatsappCliente = pedido.getCliente().getWhatsapp();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento()) +
                linha("Convidados", String.valueOf(pedido.getNumeroConvidados())) +
                linha("Valor acordado", "R$ " + pedido.getValor());

        String contato = whatsappCliente != null
                ? caixa("sucesso", "Entre em contato com o cliente para finalizar os detalhes.<br>WhatsApp: <strong>" + whatsappCliente + "</strong>")
                : caixa("sucesso", "Entre em contato com o cliente pela plataforma para finalizar os detalhes.");

        return construir(
                "Orçamento aceito!",
                "Parabéns, " + nomePrestador + "!",
                "<strong>" + nomeCliente + "</strong> aceitou o seu orçamento. O evento está confirmado!",
                detalhes,
                contato
        );
    }

    public static String pedidoCanceladoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento());

        return construir(
                "Pedido cancelado",
                "Olá, " + nomePrestador + ".",
                "<strong>" + nomeCliente + "</strong> cancelou o pedido para o seguinte evento:",
                detalhes,
                ""
        );
    }

    public static String eventoReservadoCanceladoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento()) +
                linha("Motivo", pedido.getMotivoCancelamento());

        String rodape = caixa("aviso", "A data voltou a ficar livre na sua agenda.");

        return construir(
                "Evento cancelado pelo cliente",
                "Olá, " + nomePrestador + ".",
                "<strong>" + nomeCliente + "</strong> cancelou um evento que já estava reservado:",
                detalhes,
                rodape
        );
    }

    public static String eventoReservadoCanceladoParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento()) +
                linha("Motivo", pedido.getMotivoCancelamento());

        String rodape = caixa("aviso", "Você pode procurar outro prestador para essa data na Resenha.");

        return construir(
                "Evento cancelado pelo parceiro",
                "Olá, " + nomeCliente + ".",
                "<strong>" + nomePrestador + "</strong> precisou cancelar um evento que já estava reservado:",
                detalhes,
                rodape
        );
    }

    public static String eventoCanceladoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento());

        return construir(
                "Evento cancelado",
                "Olá, " + nomePrestador + ".",
                "<strong>" + nomeCliente + "</strong> cancelou o evento. O pedido vinculado foi encerrado automaticamente:",
                detalhes,
                ""
        );
    }

    public static String orcamentoExpiradoParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Orçamento expirou em", pedido.getValidadeOrcamento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "Você pode solicitar um novo orçamento a este ou a outros prestadores diretamente na Resenha.");

        return construir(
                "Orçamento expirado",
                "Olá, " + nomeCliente + ".",
                "O orçamento enviado por <strong>" + nomePrestador + "</strong> para o seu evento atingiu a data de validade e expirou automaticamente.",
                detalhes,
                rodape
        );
    }
    public static String reservaConfirmadaParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String whatsappPrestador = pedido.getPrestador().getWhatsapp();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento()) +
                linha("Sinal pago", "R$ " + pedido.getValorSinal()) +
                linha("Valor do orçamento", "R$ " + pedido.getValor());

        String contato = whatsappPrestador != null
                ? caixa("sucesso", "A data está reservada! Fale com o prestador para alinhar os detalhes.<br>WhatsApp: <strong>" + whatsappPrestador + "</strong>")
                : caixa("sucesso", "A data está reservada! Combine os detalhes com o prestador pela plataforma.");

        return construir(
                "Data reservada!",
                "Tudo certo, " + nomeCliente + "!",
                "Recebemos o sinal e a sua data com <strong>" + nomePrestador + "</strong> está garantida.",
                detalhes,
                contato
        );
    }

    public static String dataReservadaParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "Essa data acabou de ser reservada por outro cliente. Você pode escolher outra data ou outro prestador na Resenha.");

        return construir(
                "Essa data não está mais disponível",
                "Olá, " + nomeCliente + ".",
                "O horário que você pediu com <strong>" + nomePrestador + "</strong> foi reservado por outro cliente, então este pedido foi encerrado.",
                detalhes,
                rodape
        );
    }

    public static String lembreteParaCliente(Pedido pedido, String quando) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento());

        return construir(
                "Seu evento é " + quando,
                "Olá, " + nomeCliente + "!",
                "Passando para lembrar: o seu evento com <strong>" + nomePrestador + "</strong> é " + quando + ".",
                detalhes,
                caixa("info", "Qualquer ajuste, fale com o prestador pela Resenha.")
        );
    }

    public static String lembreteParaPrestador(Pedido pedido, String quando) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Local", pedido.getLocalEvento());

        return construir(
                "Você tem um evento " + quando,
                "Olá, " + nomePrestador + "!",
                "Lembrete: o evento de <strong>" + nomeCliente + "</strong> é " + quando + ".",
                detalhes,
                caixa("info", "Prepare tudo e combine os últimos detalhes com o cliente.")
        );
    }

    public static String sinalPendenteParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Valor do orçamento", "R$ " + pedido.getValor()) +
                linha("Sinal para reservar", "R$ " + pedido.getValorSinal()) +
                linha("Pague até", pedido.getPrazoEntrada().format(FORMATO_DATA));

        String rodape = caixa("info",
                "A data só é reservada depois que o sinal é pago. Acesse a Resenha para pagar o sinal até " +
                "<strong>" + pedido.getPrazoEntrada().format(FORMATO_DATA) + "</strong>; passado o prazo, o pedido é encerrado.");

        return construir(
                "Falta pouco para garantir a data",
                "Olá, " + nomeCliente + "!",
                "Você aceitou o orçamento de <strong>" + nomePrestador + "</strong>. Para reservar a data, pague o sinal:",
                detalhes,
                rodape
        );
    }

    public static String entradaNaoPagaParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "A data não ficou reservada. Se ainda quiser contratar, é só solicitar um novo orçamento na Resenha.");

        return construir(
                "Reserva não efetivada",
                "Olá, " + nomeCliente + ".",
                "O prazo para pagar o sinal do orçamento de <strong>" + nomePrestador + "</strong> expirou, e o pedido foi encerrado automaticamente.",
                detalhes,
                rodape
        );
    }

    public static String prestadorNaoRespondeuParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                        linha("Tipo de evento", pedido.getTipoEvento()) +
                        linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "Não desanime! Você pode enviar o pedido para outros prestadores disponíveis na Resenha.");

        return construir(
                "Prestador não respondeu a tempo",
                "Olá, " + nomeCliente + ".",
                "Infelizmente <strong>" + nomePrestador + "</strong> não respondeu ao seu pedido dentro do prazo, e ele foi encerrado automaticamente.",
                detalhes,
                rodape
        );
    }
    public static String prazoPerdidoParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                        linha("Tipo de evento", pedido.getTipoEvento()) +
                        linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                        linha("Local", pedido.getLocalEvento());

        String rodape = caixa("aviso",
                "Responder rápido aumenta suas chances de fechar negócio. Fique de olho nos novos pedidos.");

        return construir(
                "Você perdeu o prazo de um pedido",
                "Olá, " + nomePrestador + ".",
                "O prazo para responder ao pedido de <strong>" + nomeCliente + "</strong> expirou, e ele foi encerrado automaticamente.",
                detalhes,
                rodape
        );
    }
    public static String reservaLiberadaParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                        linha("Tipo de evento", pedido.getTipoEvento()) +
                        linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                        linha("Valor orçado", "R$ " + pedido.getValor());

        String rodape = caixa("aviso",
                "A data voltou a ficar disponível na sua agenda. Continue enviando orçamentos para novos pedidos.");

        return construir(
                "Orçamento não aceito no prazo",
                "Olá, " + nomePrestador + ".",
                "<strong>" + nomeCliente + "</strong> não aceitou o seu orçamento dentro do prazo de validade, e a data foi liberada na sua agenda.",
                detalhes,
                rodape
        );
    }
    public static String orcamentoRecusadoPeloClienteParaPrestador(Pedido pedido) {
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String detalhes =
                linha("Cliente", nomeCliente) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Valor orçado", "R$ " + pedido.getValor());

        String rodape = caixa("aviso",
                "A data voltou a ficar disponível na sua agenda. Continue enviando orçamentos para novos pedidos.");

        return construir(
                "Orçamento recusado",
                "Olá, " + nomePrestador + ".",
                "<strong>" + nomeCliente + "</strong> recusou o orçamento enviado para o evento abaixo.",
                detalhes,
                rodape
        );
    }

    public static String midiaRemovidaPorModeracao(String nomePrestador, String tipoMidia, String tituloItem) {
        String detalhes =
                linha("Tipo de mídia", tipoMidia) +
                linha("Item associado", tituloItem);

        String rodape = caixa("aviso",
                "A remoção foi feita por nossa equipe de moderação por não estar de acordo com as diretrizes da plataforma. " +
                "Em caso de dúvidas, entre em contato com o suporte da Resenha.");

        return construir(
                "Conteúdo removido por moderação",
                "Olá, " + nomePrestador + ".",
                "Uma mídia da sua vitrine foi removida pela equipe de moderação da Resenha.",
                detalhes,
                rodape
        );
    }

    public static String itemRemovidoPorModeracao(String nomePrestador, String tituloItem) {
        String detalhes =
                linha("Item removido", tituloItem);

        String rodape = caixa("aviso",
                "A remoção foi feita por nossa equipe de moderação por não estar de acordo com as diretrizes da plataforma. " +
                "Em caso de dúvidas, entre em contato com o suporte da Resenha.");

        return construir(
                "Item removido por moderação",
                "Olá, " + nomePrestador + ".",
                "Um item do seu catálogo foi removido pela equipe de moderação da Resenha.",
                detalhes,
                rodape
        );
    }

    // --- Construtores internos ---

    private static String construir(String titulo, String saudacao, String mensagem, String linhasDetalhes, String rodapeExtra) {
        return "<!DOCTYPE html>" +
                "<html lang='pt-BR'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                "<title>Resenha</title></head>" +
                // Identidade Resenha (estilo Shop): fundo cinza-claro, card branco sem borda, marca em violeta
                "<body style='margin:0;padding:0;background-color:#f2f4f5;font-family:Inter,Arial,Helvetica,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f2f4f5;padding:32px 16px;'>" +
                "<tr><td align='center'>" +

                // Marca
                "<table width='600' cellpadding='0' cellspacing='0'>" +
                "<tr><td style='padding:0 8px 16px 8px;'>" +
                "<span style='color:#5433eb;font-size:28px;font-weight:600;letter-spacing:-1.5px;'>resenha</span>" +
                "</td></tr></table>" +

                // Card
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:28px;" +
                "box-shadow:rgba(0,0,0,0.1) 0px 4px 6px -1px,rgba(0,0,0,0.1) 0px 2px 4px -2px;'>" +

                // Título
                "<tr><td style='padding:32px 32px 0 32px;'>" +
                "<h2 style='margin:0 0 16px 0;color:#000000;font-size:20px;font-weight:600;letter-spacing:-1px;'>" + esc(titulo) + "</h2>" +
                "<p style='margin:0 0 12px 0;color:#332f2d;font-size:15px;line-height:1.6;'>" + saudacao + "</p>" +
                "<p style='margin:0 0 24px 0;color:#332f2d;font-size:15px;line-height:1.6;'>" + mensagem + "</p>" +
                "</td></tr>" +

                // Detalhes
                "<tr><td style='padding:0 32px;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f2f4f5;border-radius:20px;margin-bottom:24px;'>" +
                linhasDetalhes +
                "</table>" +
                "</td></tr>" +

                // Rodapé extra (caixa de aviso/sucesso/info)
                (rodapeExtra.isEmpty() ? "" : "<tr><td style='padding:0 32px 24px 32px;'>" + rodapeExtra + "</td></tr>") +

                "<tr><td style='padding:0 0 8px 0;'></td></tr>" +
                "</table>" +

                // Footer
                "<table width='600' cellpadding='0' cellspacing='0'>" +
                "<tr><td style='padding:20px 8px;text-align:center;'>" +
                "<p style='margin:0;color:#787574;font-size:12px;line-height:1.5;'>" +
                "Este e-mail foi enviado automaticamente pela Resenha.<br>Por favor, não responda a esta mensagem." +
                "</p></td></tr></table>" +

                "</td></tr></table></body></html>";
    }

    private static String linha(String label, String valor) {
        return "<tr>" +
                "<td style='padding:12px 20px;color:#787574;font-size:13px;width:38%;border-bottom:1px solid #ebebeb;'>" + esc(label) + "</td>" +
                "<td style='padding:12px 20px;color:#000000;font-size:13px;font-weight:500;border-bottom:1px solid #ebebeb;'>" + esc(valor) + "</td>" +
                "</tr>";
    }

    /** Caixa neutra com um ponto colorido: a cor do tipo fica só no marcador, como no app. */
    private static String caixa(String tipo, String conteudo) {
        String corPonto = switch (tipo) {
            case "sucesso" -> "#008060";
            case "aviso"   -> "#c58b00";
            default        -> "#000000"; // info
        };
        return "<div style='background-color:#f2f4f5;border-radius:20px;padding:14px 18px;font-size:14px;color:#332f2d;line-height:1.6;'>" +
                "<span style='display:inline-block;width:8px;height:8px;border-radius:50%;background-color:" + corPonto + ";" +
                "margin-right:8px;vertical-align:middle;'></span>" +
                conteudo + "</div>";
    }

    private static String esc(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
