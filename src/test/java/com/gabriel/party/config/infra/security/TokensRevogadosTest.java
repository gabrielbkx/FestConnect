package com.gabriel.party.config.infra.security;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TokensRevogadosTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static LettuceConnectionFactory conexao;

    // "Redis fora do ar": a porta 1 desta máquina, onde nada escuta
    static LettuceConnectionFactory semRedis;

    StringRedisTemplate template;
    TokensRevogados tokensRevogados;

    @BeforeAll
    static void conectar() {
        conexao = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        conexao.afterPropertiesSet();

        semRedis = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration("localhost", 1),
                LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(500)).build());
        semRedis.afterPropertiesSet();
    }

    @AfterAll
    static void desconectar() {
        conexao.destroy();
        semRedis.destroy();
    }

    @BeforeEach
    void preparar() {
        template = new StringRedisTemplate(conexao);
        tokensRevogados = new TokensRevogados(template);
    }

    private static String idNovo() {
        return UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("token cancelado passa a constar como revogado; os outros não")
    void revogar_marcaSoAqueleToken() {
        String cancelado = idNovo();
        String outro = idNovo();

        tokensRevogados.revogar(cancelado, Instant.now().plus(Duration.ofHours(2)));

        assertThat(tokensRevogados.estaRevogado(cancelado)).isTrue();
        assertThat(tokensRevogados.estaRevogado(outro)).isFalse();
    }

    @Test
    @DisplayName("a chave no Redis vive só até a hora em que o token venceria")
    void revogar_prazoIgualAoQueFaltaParaVencer() {
        String id = idNovo();

        tokensRevogados.revogar(id, Instant.now().plus(Duration.ofMinutes(30)));

        assertThat(template.getExpire("jwt:revogado:" + id)).isBetween(1_700L, 1_800L);
    }

    @Test
    @DisplayName("vale para as outras cópias da aplicação (a lista mora no Redis, não na memória)")
    void revogar_valeParaOutraCopia() {
        String id = idNovo();
        tokensRevogados.revogar(id, Instant.now().plus(Duration.ofHours(1)));

        var outraCopia = new TokensRevogados(template);

        assertThat(outraCopia.estaRevogado(id)).isTrue();
    }

    @Test
    @DisplayName("token já vencido não é guardado (não valeria de qualquer jeito)")
    void revogar_tokenVencido_naoGuarda() {
        String id = idNovo();

        tokensRevogados.revogar(id, Instant.now().minusSeconds(10));

        assertThat(template.hasKey("jwt:revogado:" + id)).isFalse();
    }

    @Test
    @DisplayName("com o Redis fora: o que esta cópia cancelou continua barrado, e o resto passa (fail-open)")
    void redisFora_lembraOsCanceladosAquiEDeixaPassarORestante() {
        var semRedisAqui = new TokensRevogados(new StringRedisTemplate(semRedis));
        String canceladoAqui = idNovo();

        semRedisAqui.revogar(canceladoAqui, Instant.now().plus(Duration.ofHours(1)));

        assertThat(semRedisAqui.estaRevogado(canceladoAqui)).isTrue();
        assertThat(semRedisAqui.estaRevogado(idNovo())).isFalse();
    }

    @Test
    @DisplayName("token sem id (anterior a esta versão) nunca consta como revogado")
    void semId_naoRevogado() {
        assertThat(tokensRevogados.estaRevogado(null)).isFalse();
    }
}
