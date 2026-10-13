package com.gabriel.party.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigCorsTest {

    private static final String FRONTEND = "http://localhost:5173";

    private CorsConfiguration configuracaoCors(List<String> origens) {
        var securityConfig = new SecurityConfig(null, null);
        ReflectionTestUtils.setField(securityConfig, "origensPermitidas", origens);
        var request = new MockHttpServletRequest("GET", "/api/v1/categorias");
        return securityConfig.corsConfigurationSource().getCorsConfiguration(request);
    }

    @Test
    @DisplayName("o frontend configurado é aceito")
    void frontendConhecido_aceito() {
        assertThat(configuracaoCors(List.of(FRONTEND)).checkOrigin(FRONTEND)).isEqualTo(FRONTEND);
    }

    @Test
    @DisplayName("origem desconhecida é recusada (antes: \"*\" aceitava qualquer site)")
    void origemDesconhecida_recusada() {
        assertThat(configuracaoCors(List.of(FRONTEND)).checkOrigin("https://site-malicioso.com")).isNull();
    }

    @Test
    @DisplayName("aceita mais de uma origem (por exemplo, produção e um preview)")
    void variasOrigens() {
        var config = configuracaoCors(List.of(FRONTEND, "https://festconnect.vercel.app"));

        assertThat(config.checkOrigin("https://festconnect.vercel.app")).isEqualTo("https://festconnect.vercel.app");
        assertThat(config.checkOrigin("https://outro.vercel.app")).isNull();
    }
}
