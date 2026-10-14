package com.gabriel.party.config.infra.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class DisjuntorDoRedisTest {

    private static final long PAUSA = DisjuntorDoRedis.PAUSA_DEPOIS_DE_FALHA.toMillis();

    // Relógio de mentira: o teste avança o tempo na mão, sem Thread.sleep
    private final AtomicLong agora = new AtomicLong(1_000_000);
    private final DisjuntorDoRedis disjuntor = new DisjuntorDoRedis(agora::get);

    private void falhar() {
        disjuntor.falhou(new QueryTimeoutException("Redis não respondeu"));
    }

    @Test
    @DisplayName("com o Redis respondendo, todo mundo pode usar")
    void normal_podeUsar() {
        assertThat(disjuntor.podeUsar()).isTrue();
        assertThat(disjuntor.podeUsar()).isTrue();
    }

    @Test
    @DisplayName("depois de uma falha, ninguém chama o Redis durante a pausa (vai direto para o plano B)")
    void depoisDeFalhar_pausa() {
        falhar();

        assertThat(disjuntor.podeUsar()).isFalse();
        agora.addAndGet(PAUSA - 1);
        assertThat(disjuntor.podeUsar()).isFalse();
    }

    @Test
    @DisplayName("passada a pausa, só UMA chamada testa o Redis; as outras seguem no plano B")
    void depoisDaPausa_soUmaTesta() {
        falhar();
        agora.addAndGet(PAUSA);

        assertThat(disjuntor.podeUsar()).isTrue();
        assertThat(disjuntor.podeUsar()).isFalse();
    }

    @Test
    @DisplayName("se o teste der certo, volta ao normal")
    void testeDeuCerto_voltaAoNormal() {
        falhar();
        agora.addAndGet(PAUSA);
        disjuntor.podeUsar();

        disjuntor.deuCerto();

        assertThat(disjuntor.podeUsar()).isTrue();
        assertThat(disjuntor.podeUsar()).isTrue();
    }

    @Test
    @DisplayName("se o teste falhar, começa outra pausa inteira")
    void testeFalhou_outraPausa() {
        falhar();
        agora.addAndGet(PAUSA);
        disjuntor.podeUsar();

        falhar();

        agora.addAndGet(PAUSA - 1);
        assertThat(disjuntor.podeUsar()).isFalse();
        agora.addAndGet(1);
        assertThat(disjuntor.podeUsar()).isTrue();
    }
}
