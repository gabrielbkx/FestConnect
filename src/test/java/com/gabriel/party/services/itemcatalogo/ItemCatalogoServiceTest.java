package com.gabriel.party.services.itemcatalogo;

import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoRequestDTO;
import com.gabriel.party.dtos.itemcatalogo.ItemCatalogoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.itemcatalogo.ItemCatalogoMapper;
import com.gabriel.party.model.itemcatalogo.ItemCatalogo;
import com.gabriel.party.model.itemcatalogo.enums.TipoItem;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.itemcatalogo.ItemCatalogoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemCatalogoServiceTest {

    @Mock
    private ItemCatalogoRepository itemCatalogoRepository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private ItemCatalogoMapper itemCatalogoMapper;

    @InjectMocks
    private ItemCatalogoService service;

    private UUID usuarioId;
    private UUID prestadorId;
    private UUID itemId;
    private Prestador prestador;
    private ItemCatalogo item;
    private ItemCatalogoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        prestadorId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        prestador = new Prestador();
        prestador.setId(prestadorId);
        prestador.setNomeCompleto("Prestador Teste");

        item = new ItemCatalogo();
        item.setId(itemId);
        item.setTitulo("Bolo Decorado");
        item.setDescricao("Bolo personalizado para festas");
        item.setPrecoBase(new BigDecimal("150.00"));
        item.setTipo(TipoItem.PRODUTO);
        item.setAtivo(true);
        item.setPrestador(prestador);

        responseDTO = new ItemCatalogoResponseDTO(
                itemId, "Bolo Decorado", "Bolo personalizado para festas",
                new BigDecimal("150.00"), TipoItem.PRODUTO, true
        );
    }

    @Nested
    @DisplayName("criarItem")
    class CriarItem {

        @Test
        @DisplayName("Deve criar item com sucesso")
        void deveCriarItemComSucesso() {
            var dto = new ItemCatalogoRequestDTO("Bolo Decorado", "Bolo personalizado para festas",
                    new BigDecimal("150.00"), TipoItem.PRODUTO);

            when(prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(prestador));
            when(itemCatalogoMapper.toEntity(dto)).thenReturn(item);
            when(itemCatalogoRepository.save(item)).thenReturn(item);
            when(itemCatalogoMapper.toDto(item)).thenReturn(responseDTO);

            var resultado = service.criarItem(dto, usuarioId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.titulo()).isEqualTo("Bolo Decorado");
            assertThat(resultado.tipo()).isEqualTo(TipoItem.PRODUTO);
            verify(itemCatalogoRepository).save(item);
            assertThat(item.getPrestador()).isEqualTo(prestador);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            var dto = new ItemCatalogoRequestDTO("Bolo", "Desc", new BigDecimal("100"), TipoItem.PRODUTO);

            when(prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarItem(dto, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(itemCatalogoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarVitrineDoPrestador")
    class ListarVitrineDoPrestador {

        @Test
        @DisplayName("Deve listar vitrine do prestador com sucesso")
        void deveListarVitrineComSucesso() {
            when(itemCatalogoRepository.findAllByPrestadorIdAndAtivoTrue(prestadorId))
                    .thenReturn(List.of(item));
            when(itemCatalogoMapper.toDto(item)).thenReturn(responseDTO);

            var resultado = service.listarVitrineDoPrestador(prestadorId);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).titulo()).isEqualTo("Bolo Decorado");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando prestador não tem itens")
        void deveRetornarListaVazia() {
            when(itemCatalogoRepository.findAllByPrestadorIdAndAtivoTrue(prestadorId))
                    .thenReturn(List.of());

            var resultado = service.listarVitrineDoPrestador(prestadorId);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarItensCatalogo")
    class ListarItensCatalogo {

        @Test
        @DisplayName("Deve listar itens do catálogo com paginação")
        void deveListarItensComPaginacao() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogo> page = new PageImpl<>(List.of(item));

            when(itemCatalogoRepository.findAllByAtivoTrue(pageable)).thenReturn(page);
            when(itemCatalogoMapper.toDto(item)).thenReturn(responseDTO);

            Page<ItemCatalogoResponseDTO> resultado = service.listarItensCatalogo(pageable);

            assertThat(resultado.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar página vazia")
        void deveRetornarPaginaVazia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogo> pageVazia = new PageImpl<>(List.of());

            when(itemCatalogoRepository.findAllByAtivoTrue(pageable)).thenReturn(pageVazia);

            Page<ItemCatalogoResponseDTO> resultado = service.listarItensCatalogo(pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarItemPorId")
    class BuscarItemPorId {

        @Test
        @DisplayName("Deve buscar item por ID com sucesso")
        void deveBuscarItemPorIdComSucesso() {
            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.of(item));
            when(itemCatalogoMapper.toDto(item)).thenReturn(responseDTO);

            var resultado = service.buscarItemPorId(itemId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(itemId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando item não encontrado")
        void deveLancarExcecaoQuandoItemNaoEncontrado() {
            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarItemPorId(itemId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("atualizarItem")
    class AtualizarItem {

        @Test
        @DisplayName("Deve atualizar item com sucesso quando prestador é o dono")
        void deveAtualizarItemComSucesso() {
            var dto = new ItemCatalogoRequestDTO("Bolo Atualizado", "Nova descrição",
                    new BigDecimal("200.00"), TipoItem.PRODUTO);

            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.of(item));
            when(prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(prestador));
            when(itemCatalogoMapper.toDto(item)).thenReturn(responseDTO);

            var resultado = service.atualizarItem(dto, itemId, usuarioId);

            assertThat(resultado).isNotNull();
            verify(itemCatalogoMapper).atualizarItemDoDTO(dto, item);
            verify(itemCatalogoRepository).save(item);
        }

        @Test
        @DisplayName("Deve lançar exceção quando item não encontrado ao atualizar")
        void deveLancarExcecaoQuandoItemNaoEncontradoAoAtualizar() {
            var dto = new ItemCatalogoRequestDTO("Bolo", "Desc", new BigDecimal("100"), TipoItem.PRODUTO);

            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarItem(dto, itemId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO));

            verify(itemCatalogoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado ao atualizar")
        void deveLancarExcecaoQuandoPrestadorNaoEncontradoAoAtualizar() {
            var dto = new ItemCatalogoRequestDTO("Bolo", "Desc", new BigDecimal("100"), TipoItem.PRODUTO);

            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.of(item));
            when(prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarItem(dto, itemId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(itemCatalogoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não é o dono do item")
        void deveLancarExcecaoQuandoPrestadorNaoEDono() {
            var dto = new ItemCatalogoRequestDTO("Bolo", "Desc", new BigDecimal("100"), TipoItem.PRODUTO);

            var outroPrestador = new Prestador();
            outroPrestador.setId(UUID.randomUUID());

            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.of(item));
            when(prestadorRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(outroPrestador));

            assertThatThrownBy(() -> service.atualizarItem(dto, itemId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USUARIO_SEM_PERMISSAO));

            verify(itemCatalogoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar item com sucesso (soft delete)")
        void deveDeletarItemComSucesso() {
            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.of(item));

            service.deletar(itemId);

            assertThat(item.getAtivo()).isFalse();
            verify(itemCatalogoRepository).save(item);
        }

        @Test
        @DisplayName("Deve lançar exceção quando item não encontrado ao deletar")
        void deveLancarExcecaoQuandoItemNaoEncontradoAoDeletar() {
            when(itemCatalogoRepository.findByIdAndAtivoTrue(itemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(itemId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ITEM_CATALOGO_NAO_ENCONTRADO));

            verify(itemCatalogoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarItensPorRadarEBusca")
    class BuscarItensPorRadarEBusca {

        @Test
        @DisplayName("Deve buscar itens com raio informado dentro do limite")
        void deveBuscarComRaioInformado() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogoResponseDTO> pageEsperada = new PageImpl<>(List.of(responseDTO));

            when(itemCatalogoRepository.buscarItensPorTermoEProximidade("bolo", -23.5, -46.6, 20.0, pageable))
                    .thenReturn(pageEsperada);

            var resultado = service.buscarItensPorRadarEBusca("bolo", -23.5, -46.6, 20.0, pageable);

            assertThat(resultado.getContent()).hasSize(1);
            verify(itemCatalogoRepository).buscarItensPorTermoEProximidade("bolo", -23.5, -46.6, 20.0, pageable);
        }

        @Test
        @DisplayName("Deve usar raio padrão de 10km quando raio for nulo")
        void deveUsarRaioPadraoQuandoRaioNulo() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogoResponseDTO> pageEsperada = new PageImpl<>(List.of());

            when(itemCatalogoRepository.buscarItensPorTermoEProximidade("", -23.5, -46.6, 10.0, pageable))
                    .thenReturn(pageEsperada);

            service.buscarItensPorRadarEBusca(null, -23.5, -46.6, null, pageable);

            verify(itemCatalogoRepository).buscarItensPorTermoEProximidade("", -23.5, -46.6, 10.0, pageable);
        }

        @Test
        @DisplayName("Deve usar raio padrão de 10km quando raio excede 50km")
        void deveUsarRaioPadraoQuandoRaioExcede50() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogoResponseDTO> pageEsperada = new PageImpl<>(List.of());

            when(itemCatalogoRepository.buscarItensPorTermoEProximidade("festa", -23.5, -46.6, 10.0, pageable))
                    .thenReturn(pageEsperada);

            service.buscarItensPorRadarEBusca("festa", -23.5, -46.6, 100.0, pageable);

            verify(itemCatalogoRepository).buscarItensPorTermoEProximidade("festa", -23.5, -46.6, 10.0, pageable);
        }

        @Test
        @DisplayName("Deve tratar termo de busca com espaços em branco como vazio")
        void deveTratarTermoComEspacosEmBranco() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ItemCatalogoResponseDTO> pageEsperada = new PageImpl<>(List.of());

            when(itemCatalogoRepository.buscarItensPorTermoEProximidade("", -23.5, -46.6, 10.0, pageable))
                    .thenReturn(pageEsperada);

            service.buscarItensPorRadarEBusca("   ", -23.5, -46.6, null, pageable);

            verify(itemCatalogoRepository).buscarItensPorTermoEProximidade("", -23.5, -46.6, 10.0, pageable);
        }
    }
}
