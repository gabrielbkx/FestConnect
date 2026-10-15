package com.gabriel.party.model.evento.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class TipoEventoTest {

    @Test
    @DisplayName("o rótulo é legível, não o name() cru do enum")
    void getRotulo_eLegivel() {
        assertThat(TipoEvento.ANIVERSARIO.getRotulo()).isEqualTo("Aniversário");
        assertThat(TipoEvento.QUINZE_ANOS.getRotulo()).isEqualTo("15 Anos");
        assertThat(TipoEvento.CHA_REVELACAO.getRotulo()).isEqualTo("Chá Revelação");
    }

    @ParameterizedTest
    @EnumSource(TipoEvento.class)
    @DisplayName("todo tipo tem rótulo e ele nunca é o name() cru em maiúsculas")
    void getRotulo_todosPreenchidosENaoCrus(TipoEvento tipo) {
        assertThat(tipo.getRotulo()).isNotBlank();
        assertThat(tipo.getRotulo()).isNotEqualTo(tipo.name());
    }
}
