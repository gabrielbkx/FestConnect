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
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class LimitadorDeTentativasTest {

    // Um Redis de verdade e descartável. A porta no seu Windows é sorteada,
    // então não briga com o Redis do docker compose na 6379.
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static LettuceConnectionFactory conexao;

    StringRedisTemplate template;
    LimitadorDeTentativas limitador;

    @BeforeAll
    static void conectar() {
        conexao = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        conexao.afterPropertiesSet();
    }

    @AfterAll
    static void desconectar() {
        conexao.destroy();
    }

    @BeforeEach
    void preparar() {
        template = new StringRedisTemplate(conexao);
        limitador = new LimitadorDeTentativas(template);
    }

    /** Cada teste usa uma chave nova: assim um teste nunca enxerga a sujeira do outro. */
    private String chaveNova() {
        return "rl:teste:" + UUID.randomUUID();
    }

    @Test
    @DisplayName("deixa as 5 primeiras tentativas passarem e barra a 6ª")
    void cincoPassam_sextaBarrada() {
        String chave = chaveNova();
        for (int i = 1; i <= 5; i++) {
            assertThat(limitador.registrar(chave, 5, Duration.ofMinutes(15)).permitido())
                    .as("tentativa %d", i)
                    .isTrue();
        }
        assertThat(limitador.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isFalse();
    }

    @Test
    @DisplayName("a chave já nasce com prazo (a pegadinha do INCR sem EXPIRE)")
    void primeiraTentativa_jaNasceComPrazo() {
        String chave = chaveNova();

        limitador.registrar(chave, 5, Duration.ofMinutes(15));

        assertThat(template.getExpire(chave)).isBetween(1L, 900L);   // -1 seria "sem prazo"
    }

    @Test
    @DisplayName("uma chave que ficou sem prazo (como a da Ana no passo 2) ganha prazo na próxima tentativa")
    void chaveSemPrazo_ganhaPrazo() {
        String chave = chaveNova();
        template.opsForValue().set(chave, "63");   // simula a chave órfã: valor sem EXPIRE

        limitador.registrar(chave, 5, Duration.ofMinutes(15));

        assertThat(template.getExpire(chave)).isBetween(1L, 900L);
    }
    @Test
    @DisplayName("chaves diferentes têm contagens separadas")
    void chavesDiferentes_naoSeMisturam() {
        String chaveA = chaveNova();
        String chaveB = chaveNova();
        for (int i = 1; i <= 6; i++) {
            limitador.registrar(chaveA, 5, Duration.ofMinutes(15));   // esgota a A
        }

        var resultadoB = limitador.registrar(chaveB, 5, Duration.ofMinutes(15));

        assertThat(resultadoB.permitido()).isTrue();
    }

    @Test
    @DisplayName("zerar apaga a contagem e libera a próxima tentativa")
    void zerar_recomecaAContagem() {
        String chave = chaveNova();
        for (int i = 1; i <= 6; i++) {
            limitador.registrar(chave, 5, Duration.ofMinutes(15));    // chega no bloqueio
        }

        limitador.zerar(chave);
        var depois = limitador.registrar(chave, 5, Duration.ofMinutes(15));

        assertThat(depois.permitido()).isTrue();
        assertThat(template.opsForValue().get(chave)).isEqualTo("1");  // recomeçou do 1
    }

    @Test
    @DisplayName("informa quanto falta para a janela acabar (vira o Retry-After)")
    void informaOTempoRestante() {
        String chave = chaveNova();

        var resultado = limitador.registrar(chave, 5, Duration.ofMinutes(15));

        assertThat(resultado.tempoRestante())
                .isPositive()
                .isLessThanOrEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("com o Redis fora do ar, continua contando na memória e não quebra o login")
    void redisForaDoAr_contaNaMemoria() {
        var semRedis = new LettuceConnectionFactory(
                new RedisStandaloneConfiguration("localhost", 1),   // nada escuta aqui: conexão recusada
                LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(500)).build());
        semRedis.afterPropertiesSet();
        try {
            var limitadorSemRedis = new LimitadorDeTentativas(new StringRedisTemplate(semRedis));
            String chave = chaveNova();

            for (int i = 1; i <= 5; i++) {
                assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido())
                        .as("tentativa %d", i)
                        .isTrue();
            }
            assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isFalse();

            limitadorSemRedis.zerar(chave);   // não pode estourar exceção
            assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isTrue();
        } finally {
            semRedis.destroy();
        }
    }


}
