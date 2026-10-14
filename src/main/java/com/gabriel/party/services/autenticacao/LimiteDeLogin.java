package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.exceptions.MuitasTentativasException;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.Locale;

/** Limite de tentativas de login: 5 por e-mail e 20 por IP a cada 15 minutos. */
@Component
public class LimiteDeLogin {

    static final int MAX_POR_EMAIL = 5;
    static final int MAX_POR_IP = 20;
    static final Duration JANELA = Duration.ofMinutes(15);

    private final LimitadorDeTentativas limitador;

    public LimiteDeLogin(LimitadorDeTentativas limitador) {
        this.limitador = limitador;
    }

    /** Conta mais uma tentativa ANTES de conferir a senha e barra quem passou do limite. */
    public void registrarTentativa(String email, String ip) {
        var porEmail = limitador.registrar(chaveDoEmail(email), MAX_POR_EMAIL, JANELA);
        var porIp = limitador.registrar("rl:login:ip:" + ip, MAX_POR_IP, JANELA);

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

    /** Acertou a senha: o contador do e-mail recomeça. O do IP não, senão o atacante zeraria o próprio limite. */
    public void loginComSucesso(String email) {
        limitador.zerar(chaveDoEmail(email));
    }

    private static String chaveDoEmail(String email) {
        return "rl:login:email:" + email.trim().toLowerCase(Locale.ROOT);
    }
}