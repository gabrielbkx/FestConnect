package com.gabriel.party.config.infra.security;

import com.gabriel.party.config.infra.redis.DisjuntorDoRedis;
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
import org.springframework.data.redis.core.ValueOperations;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
        tokensRevogados = new TokensRevogados(template, new DisjuntorDoRedis());
    }

    private static String idNovo() {
        return UUID.randomUUID().toString();
    }

    private static TokenDeLogin token(String id, Instant expiraEm) {
        return new TokenDeLogin("ana@x.com", Instant.now(), id, expiraEm);
    }

    private static TokenDeLogin tokenNovo() {
        return token(idNovo(), Instant.now().plus(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("token cancelado passa a constar como revogado; os outros não")
    void revogar_marcaSoAqueleToken() {
        var cancelado = tokenNovo();
        var outro = tokenNovo();

        tokensRevogados.revogar(cancelado);

        assertThat(tokensRevogados.estaRevogado(cancelado)).isTrue();
        assertThat(tokensRevogados.estaRevogado(outro)).isFalse();
    }

    @Test
    @DisplayName("a chave no Redis vive só até a hora em que o token venceria")
    void revogar_prazoIgualAoQueFaltaParaVencer() {
        String id = idNovo();

        tokensRevogados.revogar(token(id, Instant.now().plus(Duration.ofMinutes(30))));

        assertThat(template.getExpire("jwt:revogado:" + id)).isBetween(1_700L, 1_800L);
    }

    @Test
    @DisplayName("vale para as outras cópias da aplicação (a lista mora no Redis, não na memória)")
    void revogar_valeParaOutraCopia() {
        var cancelado = tokenNovo();
        tokensRevogados.revogar(cancelado);

        var outraCopia = new TokensRevogados(template, new DisjuntorDoRedis());

        assertThat(outraCopia.estaRevogado(cancelado)).isTrue();
    }

    @Test
    @DisplayName("token já vencido não é guardado (não valeria de qualquer jeito)")
    void revogar_tokenVencido_naoGuarda() {
        String id = idNovo();

        tokensRevogados.revogar(token(id, Instant.now().minusSeconds(10)));

        assertThat(template.hasKey("jwt:revogado:" + id)).isFalse();
    }

    @Test
    @DisplayName("com o Redis fora: o que esta cópia cancelou continua barrado, e o resto passa (fail-open)")
    void redisFora_lembraOsCanceladosAquiEDeixaPassarORestante() {
        var semRedisAqui = new TokensRevogados(new StringRedisTemplate(semRedis), new DisjuntorDoRedis());
        var canceladoAqui = tokenNovo();

        semRedisAqui.revogar(canceladoAqui);

        assertThat(semRedisAqui.estaRevogado(canceladoAqui)).isTrue();
        assertThat(semRedisAqui.estaRevogado(tokenNovo())).isFalse();
    }

    @Test
    @DisplayName("token sem id (anterior a esta versão) nunca consta como revogado")
    void semId_naoRevogado() {
        assertThat(tokensRevogados.estaRevogado(token(null, Instant.now().plusSeconds(60)))).isFalse();
    }

    // ── Com um Redis de mentira (Mockito), para simular travamento e volta

    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valores = mock(ValueOperations.class);
    private final StringRedisTemplate redisFalso = mock(StringRedisTemplate.class);

    @Test
    @DisplayName("token sem id ou sem vencimento não grava nada")
    void revogar_semIdOuSemVencimento_naoGrava() {
        var comRedisFalso = new TokensRevogados(redisFalso, new DisjuntorDoRedis());

        comRedisFalso.revogar(token(null, Instant.now().plusSeconds(60)));
        comRedisFalso.revogar(token(idNovo(), null));

        verifyNoInteractions(redisFalso);
    }

    @Test
    @DisplayName("Redis pendurado: só a primeira checagem espera; as seguintes nem tentam durante a pausa")
    void redisPendurado_naoEsperaEmTodaRequisicao() {
        when(redisFalso.hasKey(anyString())).thenThrow(new QueryTimeoutException("Redis não respondeu em 2 s"));
        var comRedisFalso = new TokensRevogados(redisFalso, new DisjuntorDoRedis());

        assertThat(comRedisFalso.estaRevogado(tokenNovo())).isFalse();
        assertThat(comRedisFalso.estaRevogado(tokenNovo())).isFalse();
        assertThat(comRedisFalso.estaRevogado(tokenNovo())).isFalse();

        verify(redisFalso, times(1)).hasKey(anyString());
    }

    @Test
    @DisplayName("cancelado com o Redis fora vai para o Redis assim que ele responder (as outras cópias ficam sabendo)")
    void canceladoComRedisFora_vaiParaORedisQuandoEleVolta() {
        var disjuntorSempreTentando = mock(DisjuntorDoRedis.class);
        when(disjuntorSempreTentando.podeUsar()).thenReturn(true);
        when(redisFalso.opsForValue()).thenReturn(valores);
        var comRedisFalso = new TokensRevogados(redisFalso, disjuntorSempreTentando);
        var cancelado = tokenNovo();
        String chave = "jwt:revogado:" + cancelado.id();

        // Redis fora: a gravação falha
        doThrow(new QueryTimeoutException("fora")).when(valores).set(eq(chave), eq("1"), any(Duration.class));
        comRedisFalso.revogar(cancelado);

        // Redis voltou: a próxima checagem (de qualquer token) responde e o cancelado é reenviado
        doNothing().when(valores).set(eq(chave), eq("1"), any(Duration.class));
        when(redisFalso.hasKey(anyString())).thenReturn(false);
        comRedisFalso.estaRevogado(tokenNovo());

        verify(valores, times(2)).set(eq(chave), eq("1"), any(Duration.class));

        // Já foi: não reenvia de novo
        comRedisFalso.estaRevogado(tokenNovo());
        verify(valores, times(2)).set(eq(chave), eq("1"), any(Duration.class));
    }

    @Test
    @DisplayName("cancelado em outra cópia, confirmado pelo Redis, fica na memória: não consulta de novo nem se o Redis cair")
    void canceladoEmOutraCopia_ficaNaMemoria() {
        var cancelado = tokenNovo();
        when(redisFalso.hasKey("jwt:revogado:" + cancelado.id())).thenReturn(true);
        var comRedisFalso = new TokensRevogados(redisFalso, new DisjuntorDoRedis());

        assertThat(comRedisFalso.estaRevogado(cancelado)).isTrue();
        assertThat(comRedisFalso.estaRevogado(cancelado)).isTrue();

        verify(redisFalso, times(1)).hasKey(anyString());
    }
}
