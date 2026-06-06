package com.gabriel.party.services.midia;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.midia.enums.TipoMidia;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.midia.MidiaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MidiaServiceTest {

    @Mock private MidiaRepository repository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private ItemCatalogoRepository itemCatalogoRepository;
    @Mock private MidiaMapper mapper;
    @Mock private ArmazenamentoService armazenamentoService;
    @Mock private EmailService emailService;

    private MidiaService service;
    private Midia midia;

    @BeforeEach
    void setUp() {
        service = new MidiaService(repository, prestadorRepository, itemCatalogoRepository,
                mapper, armazenamentoService, emailService);

        var usuarioPrestador = new Usuario();
        usuarioPrestador.setEmail("prestador@test.com");

        var prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setUsuario(usuarioPrestador);

        ItemCatalogo item = new Servico();
        item.setId(UUID.randomUUID());
        item.setTitulo("Buffet Premium");
        item.setPrestador(prestador);

        midia = new Midia();
        midia.setId(UUID.randomUUID());
        midia.setUrl("https://s3/midia.jpg");
        midia.setTipo(TipoMidia.FOTO);
        midia.setItemCatalogo(item);
    }

    @Test
    @DisplayName("deletarComoModerador remove do S3, do banco e notifica o prestador, sem checar dono")
    void deletarComoModerador_removeENotifica() {
        when(repository.findById(midia.getId())).thenReturn(Optional.of(midia));

        service.deletarComoModerador(midia.getId());

        verify(armazenamentoService).deletaMidia("https://s3/midia.jpg");
        verify(repository).delete(midia);
        verify(emailService).enviarAposCommit(eq("prestador@test.com"), contains("moderação"), contains("Prestador Teste"));
        // Moderação não passa pela checagem de dono
        verifyNoInteractions(prestadorRepository);
    }

    @Test
    @DisplayName("deletarComoModerador lança exceção quando a mídia não existe")
    void deletarComoModerador_midiaInexistente() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletarComoModerador(id))
                .isInstanceOf(AppException.class);

        verifyNoInteractions(armazenamentoService);
        verifyNoInteractions(emailService);
    }
}