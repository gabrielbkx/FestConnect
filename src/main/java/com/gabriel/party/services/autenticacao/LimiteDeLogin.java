package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.EnderecoDeOrigem;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
import com.gabriel.party.exceptions.MuitasTentativasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;

/**
 * Limite de tentativas de login, com três contadores de 15 minutos:
 * <ul>
 *   <li>e-mail + IP (5): barra quem chuta senhas de uma conta, sem trancar o dono, que entra de outro IP;</li>
 *   <li>e-mail (30): barra o ataque contra uma conta vindo de vários IPs ao mesmo tempo;</li>
 *   <li>IP (20 erros): barra quem testa muitas contas a partir do mesmo lugar.</li>
 * </ul>
 * Um limite só por e-mail deixaria qualquer pessoa trancar a conta de outra com 6 senhas erradas.
 */
@Component
public class LimiteDeLogin {

    static final int MAX_POR_EMAIL_E_IP = 5;
    static final int MAX_POR_EMAIL = 30;
    static final int MAX_POR_IP = 20;
    static final Duration JANELA = Duration.ofMinutes(15);

    private final LimitadorDeTentativas limitador;

    public LimiteDeLogin(LimitadorDeTentativas limitador) {
        this.limitador = limitador;
    }

    /** Conta mais uma tentativa ANTES de conferir a senha e barra quem passou de algum limite. */
    public void registrarTentativa(String email, String ip) {
        var chaves = Chaves.de(email, ip);
        var resultado = limitador.registrar(JANELA,
                new Limite(chaves.emailEIp(), MAX_POR_EMAIL_E_IP),
                new Limite(chaves.email(), MAX_POR_EMAIL),
                new Limite(chaves.ip(), MAX_POR_IP));
        if (!resultado.permitido()) {
            throw new MuitasTentativasException(resultado.tempoRestante());
        }
    }

    /**
     * Acertou a senha: os contadores da conta recomeçam. No do IP, só devolve esta tentativa
     * (num IP compartilhado por muita gente, logins certos não podem bloquear ninguém), sem apagar
     * os erros: senão o atacante logaria numa conta própria para zerar o limite do IP.
     */
    public void loginComSucesso(String email, String ip) {
        var chaves = Chaves.de(email, ip);
        limitador.zerar(chaves.emailEIp(), chaves.email());
        limitador.devolver(chaves.ip());
    }

    private record Chaves(String emailEIp, String email, String ip) {
        static Chaves de(String email, String ip) {
            String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
            String origem = EnderecoDeOrigem.paraChave(ip);
            return new Chaves(
                    "rl:login:email-ip:" + emailNormalizado + "|" + origem,
                    "rl:login:email:" + emailNormalizado,
                    "rl:login:ip:" + origem);
        }
    }
}
