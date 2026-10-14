package com.gabriel.party.config.infra.security;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/** IP de quem fez a requisição, no formato usado nas chaves dos limites de tentativas. */
public final class EnderecoDeOrigem {

    private EnderecoDeOrigem() {
    }

    /**
     * IPv4 fica como está. IPv6 vira o prefixo /64: o provedor entrega um bloco /64 inteiro
     * (bilhões de endereços) para cada cliente, e contar endereço por endereço deixaria o
     * atacante trocar de IP a cada tentativa.
     */
    public static String paraChave(String ip) {
        if (ip == null || ip.isBlank()) {
            return "desconhecido";
        }
        if (!ip.contains(":")) {
            return ip;
        }
        try {
            InetAddress endereco = InetAddress.getByName(ip);   // literal de IP: não consulta DNS
            if (!(endereco instanceof Inet6Address)) {
                return endereco.getHostAddress();                 // IPv4 escrito no formato IPv6 (::ffff:1.2.3.4)
            }
            byte[] bytes = endereco.getAddress();
            StringBuilder prefixo = new StringBuilder();
            for (int i = 0; i < 8; i += 2) {
                prefixo.append(Integer.toHexString(((bytes[i] & 0xff) << 8) | (bytes[i + 1] & 0xff))).append(':');
            }
            return prefixo.append(":/64").toString();
        } catch (UnknownHostException e) {
            return ip;
        }
    }
}
