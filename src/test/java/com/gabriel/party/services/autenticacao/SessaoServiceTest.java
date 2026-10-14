package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenDeLogin;
import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.config.infra.security.TokensRevogados;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessaoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TokenService tokenService;
    @Mock private TokensRevogados tokensRevogados;

    @InjectMocks private SessaoService service;

    @Test
    @DisplayName("sair de todos os dispositivos grava o próximo segundo cheio (cobre o próprio segundo atual)")
    void encerrarTodas_gravaProximoSegundoCheio() {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        Instant agora = Instant.now();

        service.encerrarTodasAsSessoes(usuario);

        Instant gravado = usuario.getSessoesValidasDesde();
        assertThat(gravado).isAfter(agora);                 // todo token já emitido (iat <= agora) cai
        assertThat(gravado).isBeforeOrEqualTo(agora.plusSeconds(2));
        assertThat(gravado.getNano()).isZero();             // segundo cheio, como o "iat" do JWT
    }

    @Test
    @DisplayName("usuário que sumiu do banco não quebra com NullPointerException")
    void usuarioInexistente_lancaAppException() {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("sumiu@festconnect.com");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.encerrarTodasAsSessoes(usuario)).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("sair deste dispositivo cancela só o token da requisição, até a hora em que ele venceria")
    void encerrarEsteDispositivo_revogaOToken() {
        Instant vence = Instant.now().plusSeconds(3600);
        when(tokenService.validarTokenDeLogin("abc.def.ghi"))
                .thenReturn(new TokenDeLogin("ana@x.com", Instant.now(), "jti-deste-aparelho", vence));

        service.encerrarEsteDispositivo("abc.def.ghi");

        verify(tokensRevogados).revogar("jti-deste-aparelho", vence);
    }
}
