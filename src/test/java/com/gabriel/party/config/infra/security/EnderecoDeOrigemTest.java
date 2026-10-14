package com.gabriel.party.config.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnderecoDeOrigemTest {

    @Test
    @DisplayName("IPv4 fica como está")
    void ipv4_semMudanca() {
        assertThat(EnderecoDeOrigem.paraChave("200.150.10.7")).isEqualTo("200.150.10.7");
    }

    @Test
    @DisplayName("dois IPv6 do mesmo bloco /64 viram a mesma chave (trocar o final não escapa do limite)")
    void ipv6_mesmoBloco_mesmaChave() {
        String a = EnderecoDeOrigem.paraChave("2001:db8:abcd:12:1111:2222:3333:4444");
        String b = EnderecoDeOrigem.paraChave("2001:db8:abcd:12:9999:8888:7777:6666");

        assertThat(a).isEqualTo(b).isEqualTo("2001:db8:abcd:12::/64");
    }

    @Test
    @DisplayName("IPv6 de blocos /64 diferentes viram chaves diferentes")
    void ipv6_blocosDiferentes() {
        assertThat(EnderecoDeOrigem.paraChave("2001:db8:abcd:12::1"))
                .isNotEqualTo(EnderecoDeOrigem.paraChave("2001:db8:abcd:13::1"));
    }

    @Test
    @DisplayName("IPv4 escrito no formato IPv6 vira o IPv4")
    void ipv4MapeadoEmIpv6() {
        assertThat(EnderecoDeOrigem.paraChave("::ffff:200.150.10.7")).isEqualTo("200.150.10.7");
    }

    @Test
    @DisplayName("sem IP, usa uma chave fixa em vez de estourar")
    void semIp() {
        assertThat(EnderecoDeOrigem.paraChave(null)).isEqualTo("desconhecido");
        assertThat(EnderecoDeOrigem.paraChave(" ")).isEqualTo("desconhecido");
    }
}
