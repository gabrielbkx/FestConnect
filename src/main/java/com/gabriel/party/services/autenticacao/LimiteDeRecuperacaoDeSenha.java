package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.exceptions.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * Limite de pedidos de código de recuperação de senha: 3 por e-mail e 10 por IP a cada hora.
 * O miolo é igual ao do LimiteDeLogin de propósito (regra de três): se surgir um terceiro
 * limite parecido, aí vale extrair o que os três têm em comum.
 */
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
        var porEmail = limitador.registrar(
                "rl:recuperacao:email:" + email.trim().toLowerCase(Locale.ROOT), MAX_POR_EMAIL, JANELA);
        var porIp = limitador.registrar("rl:recuperacao:ip:" + ip, MAX_POR_IP, JANELA);

        Duration espera = Duration.ZERO;
        if (!porEmail.permitido()) {
            espera = porEmail.tempoRestante();
        }
        if (!porIp.permitido() && porIp.tempoRestante().compareTo(espera) > 0) {
            espera = porIp.tempoRestante();
        }
        if (!porEmail.permitido() || !porIp.permitido()) {
            throw new MuitasTentativasException(espera);
        }
    }
}
