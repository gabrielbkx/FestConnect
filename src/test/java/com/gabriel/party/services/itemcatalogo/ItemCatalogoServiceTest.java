package com.gabriel.party.services.itemcatalogo;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.Servico;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.email.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemCatalogoServiceTest {

    @Mock private ItemCatalogoRepository itemCatalogoRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ItemCatalogoMapper itemCatalogoMapper;
    @Mock private AvaliacaoRepository avaliacaoRepository;
    @Mock private EmailService emailService;

    private ItemCatalogoService service;
    private ItemCatalogo item;

    @BeforeEach
    void setUp() {
        service = new ItemCatalogoService(itemCatalogoRepository, prestadorRepository,
                categoriaRepository, itemCatalogoMapper, avaliacaoRepository, emailService);

        var usuarioPrestador = new Usuario();
        usuarioPrestador.setEmail("prestador@test.com");

        var prestador = new Prestador();
        prestador.setId(UUID.randomUUID());
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setUsuario(usuarioPrestador);

        item = new Servico();
        item.setId(UUID.randomUUID());
        item.setTitulo("Show Pirotécnico");
        item.setAtivo(true);
        item.setPrestador(prestador);
    }

    @Test
    @DisplayName("removerComoModerador inativa o item e notifica o prestador, sem checar dono")
    void removerComoModerador_inativaENotifica() {
        when(itemCatalogoRepository.findByIdAndAtivoTrue(item.getId())).thenReturn(Optional.of(item));

        service.removerComoModerador(item.getId());

        assertThat(item.getAtivo()).isFalse();
        verify(itemCatalogoRepository).save(item);
        verify(emailService).enviarAposCommit(eq("prestador@test.com"), contains("moderação"), contains("Show Pirotécnico"));
        // Moderação não passa pela checagem de dono
        verifyNoInteractions(prestadorRepository);
    }

    @Test
    @DisplayName("removerComoModerador lança exceção quando o item não existe ou já está inativo")
    void removerComoModerador_itemInexistente() {
        var id = UUID.randomUUID();
        when(itemCatalogoRepository.findByIdAndAtivoTrue(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removerComoModerador(id))
                .isInstanceOf(AppException.class);

        verifyNoInteractions(emailService);
    }
}
