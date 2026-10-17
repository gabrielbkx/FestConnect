package com.gabriel.party.services.agenda;

import com.gabriel.party.dtos.agenda.BloqueioRequestDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.agenda.BloqueioAgenda;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.agenda.BloqueioAgendaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BloqueioAgendaServiceTest {

    @Mock private BloqueioAgendaRepository bloqueioRepository;
    @Mock private PrestadorRepository prestadorRepository;

    @InjectMocks private BloqueioAgendaService service;

    private Usuario usuario;
    private Prestador prestador;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("cria o bloqueio quando o intervalo é válido")
    void criar_intervaloValido_salva() {
        when(prestadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(prestador));
        when(bloqueioRepository.save(any(BloqueioAgenda.class))).thenAnswer(inv -> inv.getArgument(0));
        var dto = new BloqueioRequestDTO(LocalDateTime.of(2026, 12, 20, 18, 0), LocalDateTime.of(2026, 12, 20, 23, 0), "Evento particular");

        var resposta = service.criar(dto, usuario);

        assertThatCode(() -> resposta.inicio()).doesNotThrowAnyException();
        verify(bloqueioRepository).save(any(BloqueioAgenda.class));
    }

    @Test
    @DisplayName("recusa bloqueio com fim antes ou igual ao início")
    void criar_intervaloInvalido_barra() {
        when(prestadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(prestador));
        var dto = new BloqueioRequestDTO(LocalDateTime.of(2026, 12, 20, 23, 0), LocalDateTime.of(2026, 12, 20, 18, 0), null);

        assertThatThrownBy(() -> service.criar(dto, usuario))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BLOQUEIO_INTERVALO_INVALIDO);
        verify(bloqueioRepository, never()).save(any());
    }

    @Test
    @DisplayName("excluir bloqueio de outro prestador é proibido")
    void excluir_deOutroDono_barra() {
        var deOutro = new Prestador();
        deOutro.setId(UUID.randomUUID());
        var bloqueio = new BloqueioAgenda();
        bloqueio.setId(UUID.randomUUID());
        bloqueio.setPrestador(deOutro);
        when(prestadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(prestador));
        when(bloqueioRepository.findById(bloqueio.getId())).thenReturn(Optional.of(bloqueio));

        assertThatThrownBy(() -> service.excluir(bloqueio.getId(), usuario))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BLOQUEIO_SEM_PERMISSAO);
        verify(bloqueioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("excluir um bloqueio inexistente dá 404")
    void excluir_inexistente_404() {
        var id = UUID.randomUUID();
        when(prestadorRepository.findByUsuarioId(usuario.getId())).thenReturn(Optional.of(prestador));
        when(bloqueioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluir(id, usuario))
                .isInstanceOf(AppException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BLOQUEIO_NAO_ENCONTRADO);
    }
}
