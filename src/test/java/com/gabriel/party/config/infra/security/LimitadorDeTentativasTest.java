package com.gabriel.party.config.infra.security;

import com.gabriel.party.config.infra.redis.DisjuntorDoRedis;
import com.gabriel.party.config.infra.security.LimitadorDeTentativas.Limite;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

@Testcontainers
class LimitadorDeTentativasTest {

    // Um Redis de verdade e descartável. A porta no seu Windows é sorteada,
    // então não briga com o Redis do docker compose na 6379.
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static LettuceConnectionFactory conexao;

    // "Redis fora do ar": a porta 1 desta máquina, onde nada escuta (conexão recusada na hora)
    static LettuceConnectionFactory semRedis;

    StringRedisTemplate template;
    LimitadorDeTentativas limitador;
    LimitadorDeTentativas limitadorSemRedis;

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
        limitador = new LimitadorDeTentativas(template, new DisjuntorDoRedis());
        limitadorSemRedis = new LimitadorDeTentativas(new StringRedisTemplate(semRedis), new DisjuntorDoRedis());
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
    @DisplayName("vários limites numa chamada: barra quando qualquer um estoura e soma em todos")
    void variosLimites_barraPeloQueEstourou() {
        String apertado = chaveNova();
        String folgado = chaveNova();
        var janela = Duration.ofMinutes(15);

        limitador.registrar(janela, new Limite(apertado, 2), new Limite(folgado, 10));
        limitador.registrar(janela, new Limite(apertado, 2), new Limite(folgado, 10));
        var terceira = limitador.registrar(janela, new Limite(apertado, 2), new Limite(folgado, 10));

        assertThat(terceira.permitido()).isFalse();
        assertThat(terceira.tempoRestante()).isPositive().isLessThanOrEqualTo(janela);
        assertThat(template.opsForValue().get(apertado)).isEqualTo("3");
        assertThat(template.opsForValue().get(folgado)).isEqualTo("3");
    }

    @Test
    @DisplayName("devolver desconta uma tentativa sem apagar a contagem nem o prazo")
    void devolver_descontaUma() {
        String chave = chaveNova();
        limitador.registrar(chave, 20, Duration.ofMinutes(15));
        limitador.registrar(chave, 20, Duration.ofMinutes(15));
        limitador.registrar(chave, 20, Duration.ofMinutes(15));

        limitador.devolver(chave);

        assertThat(template.opsForValue().get(chave)).isEqualTo("2");
        assertThat(template.getExpire(chave)).isBetween(1L, 900L);
    }

    @Test
    @DisplayName("devolver numa chave que não existe não cria nada (nem chave negativa sem prazo)")
    void devolver_semChave_naoCriaNada() {
        String chave = chaveNova();

        limitador.devolver(chave);

        assertThat(template.hasKey(chave)).isFalse();
    }

    @Test
    @DisplayName("com o Redis fora do ar, continua contando na memória (plano B) e não quebra o login")
    void redisForaDoAr_contaNaMemoria() {
        String chave = chaveNova();

        for (int i = 1; i <= 5; i++) {
            assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido())
                    .as("tentativa %d", i)
                    .isTrue();
        }
        assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isFalse();

        assertThatCode(() -> limitadorSemRedis.zerar(chave)).doesNotThrowAnyException();
        assertThat(limitadorSemRedis.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isTrue();
    }

    @Test
    @DisplayName("no plano B, a janela acaba e a contagem recomeça")
    void planoB_janelaAcaba_libera() throws InterruptedException {
        String chave = chaveNova();
        var janelaCurta = Duration.ofMillis(300);
        limitadorSemRedis.registrar(chave, 1, janelaCurta);
        assertThat(limitadorSemRedis.registrar(chave, 1, janelaCurta).permitido()).isFalse();

        Thread.sleep(400);   // espera a janela acabar

        assertThat(limitadorSemRedis.registrar(chave, 1, janelaCurta).permitido()).isTrue();
    }

    @Test
    @DisplayName("no plano B, devolver também desconta uma tentativa")
    void planoB_devolver() {
        String chave = chaveNova();
        limitadorSemRedis.registrar(chave, 2, Duration.ofMinutes(15));
        limitadorSemRedis.registrar(chave, 2, Duration.ofMinutes(15));

        assertThatCode(() -> limitadorSemRedis.devolver(chave)).doesNotThrowAnyException();

        assertThat(limitadorSemRedis.registrar(chave, 2, Duration.ofMinutes(15)).permitido()).isTrue();
    }

    @Test
    @DisplayName("Redis pendurado: só a primeira chamada espera; as seguintes vão direto para o plano B")
    void redisPendurado_naoEsperaEmTodaTentativa() {
        // Todo método deste Redis de mentira estoura como um timeout
        var redisPendurado = mock(StringRedisTemplate.class, invocacao -> {
            throw new QueryTimeoutException("Redis não respondeu em 2 s");
        });
        var comRedisPendurado = new LimitadorDeTentativas(redisPendurado, new DisjuntorDoRedis());
        String chave = chaveNova();

        for (int i = 1; i <= 5; i++) {
            assertThat(comRedisPendurado.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isTrue();
        }
        assertThat(comRedisPendurado.registrar(chave, 5, Duration.ofMinutes(15)).permitido()).isFalse();
        comRedisPendurado.zerar(chave);
        comRedisPendurado.devolver(chave);

        assertThat(mockingDetails(redisPendurado).getInvocations()).hasSize(1);
    }
}
