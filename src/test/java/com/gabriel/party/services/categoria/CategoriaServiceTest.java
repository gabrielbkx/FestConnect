package com.gabriel.party.services.categoria;

import com.gabriel.party.mapper.categoria.CategoriaMapper;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository.ContagemDePrestadoresPorCategoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock private CategoriaRepository repository;
    @Mock private ItemCatalogoRepository itemCatalogoRepository;
    @Mock private CategoriaMapper mapper;

    @InjectMocks private CategoriaService service;

    private Categoria categoria(UUID id) {
        var c = new Categoria();
        c.setId(id);
        return c;
    }

    private ContagemDePrestadoresPorCategoria contagem(UUID categoriaId, long total) {
        var c = mock(ContagemDePrestadoresPorCategoria.class);
        when(c.getCategoriaId()).thenReturn(categoriaId);
        when(c.getTotal()).thenReturn(total);
        return c;
    }

    @Test
    @DisplayName("a listagem usa a contagem real; categoria sem prestador fica com 0")
    void listarCategorias_usaContagemRealComDefaultZero() {
        UUID idComPrestadores = UUID.randomUUID();
        UUID idSemPrestadores = UUID.randomUUID();
        var comPrestadores = categoria(idComPrestadores);
        var semPrestadores = categoria(idSemPrestadores);

        // Montado antes do when() de fora para não aninhar stubbing (UnfinishedStubbing)
        var contagemComPrestadores = contagem(idComPrestadores, 3);

        when(repository.findAllByAtivoTrue(any()))
                .thenReturn(new PageImpl<>(List.of(comPrestadores, semPrestadores)));
        // Só a categoria com prestadores volta da consulta agrupada; a outra nem aparece
        when(itemCatalogoRepository.contarPrestadoresPorCategoria(List.of(idComPrestadores, idSemPrestadores)))
                .thenReturn(List.of(contagemComPrestadores));

        service.listarCategorias(PageRequest.of(0, 20));

        verify(mapper).toDto(comPrestadores, 3);
        verify(mapper).toDto(semPrestadores, 0);
    }

    @Test
    @DisplayName("página vazia não consulta a contagem (sem query com lista vazia)")
    void listarCategorias_paginaVazia_naoConsulta() {
        when(repository.findAllByAtivoTrue(any())).thenReturn(new PageImpl<>(List.of()));

        service.listarCategorias(PageRequest.of(0, 20));

        verify(itemCatalogoRepository, org.mockito.Mockito.never()).contarPrestadoresPorCategoria(any());
    }
}
