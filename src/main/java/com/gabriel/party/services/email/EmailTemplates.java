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
                "Para aceitar ou recusar o orçamento, acesse a plataforma FestConnect. " +
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
                "Não desanime! Explore outros prestadores disponíveis na plataforma FestConnect para o seu evento.");

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

    public static String orcamentoExpiradoParaCliente(Pedido pedido) {
        String nomeCliente = pedido.getCliente().getNomeCompleto();
        String nomePrestador = pedido.getPrestador().getNomeCompleto();
        String detalhes =
                linha("Prestador", nomePrestador) +
                linha("Tipo de evento", pedido.getTipoEvento()) +
                linha("Data do evento", pedido.getDataEvento().format(FORMATO_DATA)) +
                linha("Orçamento expirou em", pedido.getValidadeOrcamento().format(FORMATO_DATA));

        String rodape = caixa("aviso",
                "Você pode solicitar um novo orçamento a este ou a outros prestadores diretamente na plataforma FestConnect.");

        return construir(
                "Orçamento expirado",
                "Olá, " + nomeCliente + ".",
                "O orçamento enviado por <strong>" + nomePrestador + "</strong> para o seu evento atingiu a data de validade e expirou automaticamente.",
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
                "Não desanime! Você pode enviar o pedido para outros prestadores disponíveis na plataforma FestConnect.");

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
    public static String midiaRemovidaPorModeracao(String nomePrestador, String tipoMidia, String tituloItem) {
        String detalhes =
                linha("Tipo de mídia", tipoMidia) +
                linha("Item associado", tituloItem);

        String rodape = caixa("aviso",
                "A remoção foi feita por nossa equipe de moderação por não estar de acordo com as diretrizes da plataforma. " +
                "Em caso de dúvidas, entre em contato com o suporte do FestConnect.");

        return construir(
                "Conteúdo removido por moderação",
                "Olá, " + nomePrestador + ".",
                "Uma mídia da sua vitrine foi removida pela equipe de moderação do FestConnect.",
                detalhes,
                rodape
        );
    }

    public static String itemRemovidoPorModeracao(String nomePrestador, String tituloItem) {
        String detalhes =
                linha("Item removido", tituloItem);

        String rodape = caixa("aviso",
                "A remoção foi feita por nossa equipe de moderação por não estar de acordo com as diretrizes da plataforma. " +
                "Em caso de dúvidas, entre em contato com o suporte do FestConnect.");

        return construir(
                "Item removido por moderação",
                "Olá, " + nomePrestador + ".",
                "Um item do seu catálogo foi removido pela equipe de moderação do FestConnect.",
                detalhes,
                rodape
        );
    }

    // --- Construtores internos ---

    private static String construir(String titulo, String saudacao, String mensagem, String linhasDetalhes, String rodapeExtra) {
        return "<!DOCTYPE html>" +
                "<html lang='pt-BR'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
                "<title>FestConnect</title></head>" +
                "<body style='margin:0;padding:0;background-color:#f3f4f6;font-family:Arial,Helvetica,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f3f4f6;padding:40px 16px;'>" +
                "<tr><td align='center'>" +
                "<table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border-radius:10px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);'>" +

                // Header
                "<tr><td style='background-color:#7c3aed;padding:28px 32px;text-align:center;'>" +
                "<span style='color:#ffffff;font-size:26px;font-weight:700;letter-spacing:-0.5px;'>Fest</span>" +
                "<span style='color:#ddd6fe;font-size:26px;font-weight:300;letter-spacing:-0.5px;'>Connect</span>" +
                "</td></tr>" +

                // Título
                "<tr><td style='padding:32px 32px 0 32px;'>" +
                "<h2 style='margin:0 0 16px 0;color:#111827;font-size:20px;font-weight:700;'>" + esc(titulo) + "</h2>" +
                "<p style='margin:0 0 12px 0;color:#374151;font-size:15px;line-height:1.6;'>" + saudacao + "</p>" +
                "<p style='margin:0 0 24px 0;color:#374151;font-size:15px;line-height:1.6;'>" + mensagem + "</p>" +
                "</td></tr>" +

                // Detalhes
                "<tr><td style='padding:0 32px;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;margin-bottom:24px;'>" +
                linhasDetalhes +
                "</table>" +
                "</td></tr>" +

                // Rodapé extra (caixa de aviso/sucesso/info)
                (rodapeExtra.isEmpty() ? "" : "<tr><td style='padding:0 32px 24px 32px;'>" + rodapeExtra + "</td></tr>") +

                // Footer
                "<tr><td style='background-color:#f9fafb;border-top:1px solid #e5e7eb;padding:20px 32px;text-align:center;'>" +
                "<p style='margin:0;color:#9ca3af;font-size:12px;line-height:1.5;'>" +
                "Este e-mail foi enviado automaticamente pela plataforma FestConnect.<br>Por favor, não responda a esta mensagem." +
                "</p></td></tr>" +

                "</table></td></tr></table></body></html>";
    }

    private static String linha(String label, String valor) {
        return "<tr>" +
                "<td style='padding:10px 16px;color:#6b7280;font-size:13px;font-weight:600;width:38%;border-bottom:1px solid #e5e7eb;'>" + esc(label) + "</td>" +
                "<td style='padding:10px 16px;color:#111827;font-size:13px;border-bottom:1px solid #e5e7eb;'>" + esc(valor) + "</td>" +
                "</tr>";
    }

    private static String caixa(String tipo, String conteudo) {
        String corBorda = switch (tipo) {
            case "sucesso" -> "#16a34a";
            case "aviso"   -> "#d97706";
            default        -> "#2563eb"; // info
        };
        String corFundo = switch (tipo) {
            case "sucesso" -> "#f0fdf4";
            case "aviso"   -> "#fffbeb";
            default        -> "#eff6ff";
        };
        return "<div style='background-color:" + corFundo + ";border-left:4px solid " + corBorda + ";" +
                "border-radius:4px;padding:14px 16px;font-size:14px;color:#374151;line-height:1.6;'>" +
                conteudo + "</div>";
    }

    private static String esc(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
