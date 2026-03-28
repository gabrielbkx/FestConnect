package com.gabriel.party.exceptions.utils;

public class FormatadorDeMensagemDeErro {

    public static String formatarMensagem(String mensagemBase, String... args) {
        String mensagemFormatada = mensagemBase;
        if (args != null && args.length > 0) {
            for (String arg : args) {
                mensagemFormatada = mensagemFormatada.replaceFirst("%[^%]+%", arg);
            }
        }
        return mensagemFormatada;
    }
}
