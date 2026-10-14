package com.gabriel.party.services.autenticacao;

import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.services.email.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificacaoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmailService emailService;

    @InjectMocks private EmailVerificacaoService service;

    @Test
    @DisplayName("gera o token e manda o e-mail depois do commit, fora da thread da requisição")
    void enviarVerificacao_enviaAposCommit() {
        var usuario = new Usuario();
        usuario.setEmail("ana@x.com");

        service.enviarVerificacaoEmail(usuario);

        assertThat(usuario.getTokenVerificacao()).isNotBlank();
        verify(usuarioRepository).save(usuario);
        verify(emailService).enviarAposCommit(eq("ana@x.com"), anyString(), contains(usuario.getTokenVerificacao()));
        // O envio direto prenderia a requisição até o servidor de e-mail responder (e o tempo revelaria a conta)
        verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
    }
}
