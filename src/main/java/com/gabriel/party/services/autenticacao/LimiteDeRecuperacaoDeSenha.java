package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.EnderecoDeOrigem;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
import com.gabriel.party.exceptions.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/** Limite de pedidos de código de recuperação de senha: 3 por e-mail e 10 por IP a cada hora. */
@Component
public class LimiteDeRecuperacaoDeSenha {

    static final int MAX_POR_EMAIL = 3;
    static final int MAX_POR_IP = 10;
    static final Duration JANELA = Duration.ofHours(1);

    private final LimitadorDeTentativas limitador;

    public LimiteDeRecuperacaoDeSenha(LimitadorDeTentativas limitador) {
        this.limitador = limitador;
    }

    /**
     * Conta pelo e-mail DIGITADO, exista ele ou não: assim o 429 aparece igual para e-mail
     * cadastrado e inventado, e a resposta não revela quem tem conta na Resenha.
     */
    public void registrarPedido(String email, String ip) {
        var resultado = limitador.registrar(JANELA,
                new Limite("rl:recuperacao:email:" + email.trim().toLowerCase(Locale.ROOT), MAX_POR_EMAIL),
                new Limite("rl:recuperacao:ip:" + EnderecoDeOrigem.paraChave(ip), MAX_POR_IP));
        if (!resultado.permitido()) {
            throw new MuitasTentativasException(resultado.tempoRestante());
        }
    }
}
