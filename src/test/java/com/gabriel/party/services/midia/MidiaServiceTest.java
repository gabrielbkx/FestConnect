package com.gabriel.party.services.midia;

import com.gabriel.party.dtos.midia.MidiaRequestDTO;
import com.gabriel.party.dtos.midia.MidiaResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.midia.MidiaMapper;
import com.gabriel.party.model.midia.Midia;
import com.gabriel.party.model.midia.enums.TipoMidia;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.repositories.midia.MidiaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MidiaServiceTest {

    @Mock
    private MidiaRepository repository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private MidiaMapper mapper;

    @Mock
    private ArmazenamentoService armazenamentoService;

    @Mock
    private MultipartFile arquivo;

    @InjectMocks
    private MidiaService service;

    private UUID prestadorId;
    private UUID midiaId;
    private Prestador prestador;
    private Midia midia;
    private MidiaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        prestadorId = UUID.randomUUID();
        midiaId = UUID.randomUUID();

        prestador = new Prestador();
        prestador.setId(prestadorId);
        prestador.setNomeCompleto("Prestador Teste");

        midia = new Midia();
        midia.setId(midiaId);
        midia.setUrl("https://bucket.s3.amazonaws.com/foto.jpg");
        midia.setTipo(TipoMidia.FOTO);
        midia.setOrdem(1);
        midia.setPrestador(prestador);

        responseDTO = new MidiaResponseDTO(midiaId, "https://bucket.s3.amazonaws.com/foto.jpg",
                TipoMidia.FOTO, 1, prestadorId, "Prestador Teste");
    }

    @Nested
    @DisplayName("salvarMidia")
    class SalvarMidia {

        @Test
        @DisplayName("Deve salvar mídia com sucesso")
        void deveSalvarMidiaComSucesso() {
            var dto = new MidiaRequestDTO(TipoMidia.FOTO, 1, prestadorId);

            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(repository.countMidiaByPrestadorId(prestadorId)).thenReturn(5L);
            when(mapper.toEntity(dto)).thenReturn(midia);
            when(armazenamentoService.salvarMidias(arquivo)).thenReturn("https://bucket.s3.amazonaws.com/foto.jpg");
            when(mapper.toDto(midia)).thenReturn(responseDTO);

            var resultado = service.salvarMidia(arquivo, dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.tipo()).isEqualTo(TipoMidia.FOTO);
            verify(repository).save(midia);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            var dto = new MidiaRequestDTO(TipoMidia.FOTO, 1, prestadorId);

            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.salvarMidia(arquivo, dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando limite de mídias atingido")
        void deveLancarExcecaoQuandoLimiteDeMidiasAtingido() {
            var dto = new MidiaRequestDTO(TipoMidia.FOTO, 1, prestadorId);

            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(repository.countMidiaByPrestadorId(prestadorId)).thenReturn(10L);

            assertThatThrownBy(() -> service.salvarMidia(arquivo, dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.LIMITE_MIDIAS_PRESTADOR));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve deletar mídia do S3 quando falhar ao salvar no banco")
        void deveDeletarMidiaDoS3QuandoFalharAoSalvarNoBanco() {
            var dto = new MidiaRequestDTO(TipoMidia.FOTO, 1, prestadorId);
            String urlMidia = "https://bucket.s3.amazonaws.com/foto.jpg";

            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(repository.countMidiaByPrestadorId(prestadorId)).thenReturn(5L);
            when(mapper.toEntity(dto)).thenReturn(midia);
            when(armazenamentoService.salvarMidias(arquivo)).thenReturn(urlMidia);
            when(repository.save(midia)).thenThrow(new RuntimeException("Erro no banco"));

            assertThatThrownBy(() -> service.salvarMidia(arquivo, dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ERRO_SALVAR_MIDIA));

            verify(armazenamentoService).deletaMidia(urlMidia);
        }
    }

    @Nested
    @DisplayName("listarMidias")
    class ListarMidias {

        @Test
        @DisplayName("Deve listar mídias com paginação e filtro")
        void deveListarMidiasComPaginacaoEFiltro() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Midia> page = new PageImpl<>(List.of(midia));

            when(repository.buscarTodasAsMidiasDoPrestador(pageable, prestadorId, TipoMidia.FOTO))
                    .thenReturn(page);
            when(mapper.toDto(midia)).thenReturn(responseDTO);

            Page<MidiaResponseDTO> resultado = service.listarMidias(pageable, prestadorId, TipoMidia.FOTO);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).tipo()).isEqualTo(TipoMidia.FOTO);
        }

        @Test
        @DisplayName("Deve listar mídias sem filtro de tipo")
        void deveListarMidiasSemFiltro() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Midia> page = new PageImpl<>(List.of(midia));

            when(repository.buscarTodasAsMidiasDoPrestador(pageable, prestadorId, null))
                    .thenReturn(page);
            when(mapper.toDto(midia)).thenReturn(responseDTO);

            Page<MidiaResponseDTO> resultado = service.listarMidias(pageable, prestadorId, null);

            assertThat(resultado.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há mídias")
        void deveRetornarPaginaVazia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Midia> pageVazia = new PageImpl<>(List.of());

            when(repository.buscarTodasAsMidiasDoPrestador(pageable, prestadorId, null))
                    .thenReturn(pageVazia);

            Page<MidiaResponseDTO> resultado = service.listarMidias(pageable, prestadorId, null);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarMidiaPorId")
    class BuscarMidiaPorId {

        @Test
        @DisplayName("Deve buscar mídia por ID com sucesso")
        void deveBuscarMidiaPorIdComSucesso() {
            when(repository.findById(midiaId)).thenReturn(Optional.of(midia));
            when(mapper.toDto(midia)).thenReturn(responseDTO);

            var resultado = service.buscarMidiaPorId(midiaId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(midiaId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando mídia não encontrada")
        void deveLancarExcecaoQuandoMidiaNaoEncontrada() {
            when(repository.findById(midiaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarMidiaPorId(midiaId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MIDIA_NAO_ENCONTRADA));
        }
    }

    @Nested
    @DisplayName("atualizarMidia")
    class AtualizarMidia {

        @Test
        @DisplayName("Deve atualizar mídia com sucesso")
        void deveAtualizarMidiaComSucesso() {
            var dto = new MidiaRequestDTO(TipoMidia.VIDEO, 2, prestadorId);

            when(repository.findById(midiaId)).thenReturn(Optional.of(midia));
            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(mapper.toDto(midia)).thenReturn(responseDTO);

            var resultado = service.atualizarMidia(dto, midiaId);

            assertThat(resultado).isNotNull();
            verify(mapper).atualizarMidiaDoDTO(dto, midia);
            verify(repository).save(midia);
        }

        @Test
        @DisplayName("Deve lançar exceção quando mídia não encontrada ao atualizar")
        void deveLancarExcecaoQuandoMidiaNaoEncontradaAoAtualizar() {
            var dto = new MidiaRequestDTO(TipoMidia.VIDEO, 2, prestadorId);

            when(repository.findById(midiaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarMidia(dto, midiaId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MIDIA_NAO_ENCONTRADA));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado ao atualizar")
        void deveLancarExcecaoQuandoPrestadorNaoEncontradoAoAtualizar() {
            var dto = new MidiaRequestDTO(TipoMidia.VIDEO, 2, prestadorId);

            when(repository.findById(midiaId)).thenReturn(Optional.of(midia));
            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarMidia(dto, midiaId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar mídia com sucesso")
        void deveDeletarMidiaComSucesso() {
            when(repository.findById(midiaId)).thenReturn(Optional.of(midia));

            service.deletar(midiaId);

            verify(armazenamentoService).deletaMidia(midia.getUrl());
            verify(repository).save(midia);
        }

        @Test
        @DisplayName("Deve lançar exceção quando mídia não encontrada ao deletar")
        void deveLancarExcecaoQuandoMidiaNaoEncontradaAoDeletar() {
            when(repository.findById(midiaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(midiaId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.MIDIA_NAO_ENCONTRADA));

            verify(armazenamentoService, never()).deletaMidia(any());
        }
    }
}
