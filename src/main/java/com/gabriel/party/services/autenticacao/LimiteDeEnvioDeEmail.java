package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.EnderecoDeOrigem;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
import com.gabriel.party.exceptions.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * Limite dos endpoints públicos que disparam e-mail: 3 pedidos por e-mail e 10 por IP a cada hora,
 * contados separadamente para cada tipo de envio. Protege a caixa de entrada das pessoas e a cota
 * de envio do sistema.
 */
@Component
public class LimiteDeEnvioDeEmail {

    /** Cada tipo de envio tem os próprios contadores (o prefixo das chaves no Redis). */
    public enum Envio {
        RECUPERACAO_DE_SENHA("recuperacao"),
        VERIFICACAO_DE_EMAIL("verificacao");

        private final String prefixo;

        Envio(String prefixo) {
            this.prefixo = prefixo;
        }
    }

    static final int MAX_POR_EMAIL = 3;
    static final int MAX_POR_IP = 10;
    static final Duration JANELA = Duration.ofHours(1);

    private final LimitadorDeTentativas limitador;

    public LimiteDeEnvioDeEmail(LimitadorDeTentativas limitador) {
        this.limitador = limitador;
    }

    /**
     * Conta pelo e-mail DIGITADO, exista ele ou não: assim o 429 aparece igual para e-mail
     * cadastrado e inventado, e a resposta não revela quem tem conta na Resenha.
     */
    public void registrarPedido(Envio envio, String email, String ip) {
        var resultado = limitador.registrar(JANELA,
                new Limite("rl:" + envio.prefixo + ":email:" + email.trim().toLowerCase(Locale.ROOT), MAX_POR_EMAIL),
                new Limite("rl:" + envio.prefixo + ":ip:" + EnderecoDeOrigem.paraChave(ip), MAX_POR_IP));
        if (!resultado.permitido()) {
            throw new MuitasTentativasException(resultado.tempoRestante());
        }
    }
}
