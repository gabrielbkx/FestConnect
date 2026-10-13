package com.gabriel.party.config;

import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarAdminTest {

    private static final String EMAIL = "admin@festconnect.com";
    private static final String SENHA_FORTE = "uma-senha-bem-longa";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private CriarAdmin criarAdmin(String email, String senha) {
        return new CriarAdmin(usuarioRepository, passwordEncoder, email, senha);
    }

    @Test
    @DisplayName("sem ADMIN_EMAIL/ADMIN_SENHA não cria admin nenhum (e a aplicação sobe)")
    void semVariaveis_naoCriaAdmin() {
        criarAdmin("", "").run();

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("só o e-mail definido, sem senha, também não cria")
    void semSenha_naoCriaAdmin() {
        criarAdmin(EMAIL, "").run();

        verifyNoInteractions(usuarioRepository, passwordEncoder);
    }

    @Test
    @DisplayName("senha com menos de 12 caracteres impede a aplicação de subir")
    void senhaCurta_falhaNaSubida() {
        var criar = criarAdmin(EMAIL, "admin");

        assertThatThrownBy(criar::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.valueOf(CriarAdmin.TAMANHO_MINIMO_SENHA));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("admin já existente não é alterado")
    void adminExistente_naoAltera() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(true);

        criarAdmin(EMAIL, SENHA_FORTE).run();

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("cria o admin com papel de administrador e a senha codificada")
    void variaveisValidas_criaAdmin() {
        when(usuarioRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(SENHA_FORTE)).thenReturn("hash-da-senha");

        criarAdmin(EMAIL, SENHA_FORTE).run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();
        assertThat(salvo.getEmail()).isEqualTo(EMAIL);
        assertThat(salvo.getRole()).isEqualTo(Role.ROLE_ADMINISTRADOR);
        assertThat(salvo.getSenha()).isEqualTo("hash-da-senha");
    }
}
