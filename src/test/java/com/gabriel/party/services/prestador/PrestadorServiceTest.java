package com.gabriel.party.services.prestador;

import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.integracoes.CoordenadasDTO;
import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.dtos.prestador.PrestadorResponseDTO;
import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.categoria.Categoria;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import com.gabriel.party.shared.Endereco;
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
class PrestadorServiceTest {

    @Mock
    private PrestadorRepository repository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private PrestadorMapper mapper;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ArmazenamentoService armazenamentoService;

    @Mock
    private MultipartFile fotoPerfil;

    @InjectMocks
    private PrestadorService service;

    private UUID prestadorId;
    private UUID categoriaId;
    private Prestador prestador;
    private Categoria categoria;
    private Usuario usuario;
    private PrestadorResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        prestadorId = UUID.randomUUID();
        categoriaId = UUID.randomUUID();

        categoria = new Categoria();
        categoria.setId(categoriaId);
        categoria.setNome("Buffet");

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setEmail("prestador@teste.com");
        usuario.setAtivo(true);

        prestador = new Prestador();
        prestador.setId(prestadorId);
        prestador.setNomeCompleto("Prestador Teste");
        prestador.setCategoria(categoria);
        prestador.setUsuario(usuario);
        prestador.setAtivo(true);
        prestador.setEndereco(new Endereco());

        responseDTO = new PrestadorResponseDTO(
                prestadorId, "Prestador Teste", "prestador@teste.com",
                "11999999999", categoriaId, "Buffet",
                new EnderecoDTO("Rua Teste", "Centro", "01001000", "São Paulo", "SP",
                        null, 100, -23.5, -46.6),
                List.of(), List.of(), List.of()
        );
    }

    @Nested
    @DisplayName("criarPerfilPrestador")
    class CriarPerfilPrestador {

        @Test
        @DisplayName("Deve criar perfil de prestador com sucesso")
        void deveCriarPerfilComSucesso() {
            var enderecoDTO = new EnderecoDTO("Rua Teste", "Centro", "01001000", "São Paulo", "SP", null, 100, null, null);
            var dto = new CadastroPrestadorDTO("Prestador", "email@teste.com", "senha123",
                    "11999999999", "12345678901234", categoriaId, enderecoDTO);

            when(categoriaRepository.findByIdAndAtivoTrue(categoriaId)).thenReturn(Optional.of(categoria));
            when(usuarioMapper.toPrestador(dto)).thenReturn(prestador);
            when(armazenamentoService.salvarMidias(fotoPerfil)).thenReturn("https://s3.amazonaws.com/foto.jpg");
            when(geocodingService.buscarCoordenadas("Rua Teste", "São Paulo", "SP"))
                    .thenReturn(new CoordenadasDTO(-23.5, -46.6));
            when(repository.save(prestador)).thenReturn(prestador);

            var resultado = service.criarPerfilPrestador(dto, usuario, fotoPerfil);

            assertThat(resultado).isNotNull();
            verify(repository).save(prestador);
            verify(armazenamentoService).salvarMidias(fotoPerfil);
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria não encontrada")
        void deveLancarExcecaoQuandoCategoriaNaoEncontrada() {
            var enderecoDTO = new EnderecoDTO("Rua Teste", "Centro", "01001000", "São Paulo", "SP", null, 100, null, null);
            var dto = new CadastroPrestadorDTO("Prestador", "email@teste.com", "senha123",
                    "11999999999", "12345678901234", categoriaId, enderecoDTO);

            when(categoriaRepository.findByIdAndAtivoTrue(categoriaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criarPerfilPrestador(dto, usuario, fotoPerfil))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORIA_NAO_ENCONTRADA));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando coordenadas não obtidas")
        void deveLancarExcecaoQuandoCoordenadasNaoObtidas() {
            var enderecoDTO = new EnderecoDTO("Rua Teste", "Centro", "01001000", "São Paulo", "SP", null, 100, null, null);
            var dto = new CadastroPrestadorDTO("Prestador", "email@teste.com", "senha123",
                    "11999999999", "12345678901234", categoriaId, enderecoDTO);

            when(categoriaRepository.findByIdAndAtivoTrue(categoriaId)).thenReturn(Optional.of(categoria));
            when(usuarioMapper.toPrestador(dto)).thenReturn(prestador);
            when(armazenamentoService.salvarMidias(fotoPerfil)).thenReturn("https://s3.amazonaws.com/foto.jpg");
            when(geocodingService.buscarCoordenadas("Rua Teste", "São Paulo", "SP")).thenReturn(null);

            assertThatThrownBy(() -> service.criarPerfilPrestador(dto, usuario, fotoPerfil))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.REGRA_NEGOCIO_VIOLADA));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarPrestadores")
    class ListarPrestadores {

        @Test
        @DisplayName("Deve listar prestadores com paginação")
        void deveListarPrestadoresComPaginacao() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Prestador> page = new PageImpl<>(List.of(prestador));

            when(repository.findAllByAtivoTrue(pageable)).thenReturn(page);
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            Page<PrestadorResponseDTO> resultado = service.listarPrestadores(pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).nome()).isEqualTo("Prestador Teste");
        }

        @Test
        @DisplayName("Deve retornar página vazia")
        void deveRetornarPaginaVazia() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Prestador> pageVazia = new PageImpl<>(List.of());

            when(repository.findAllByAtivoTrue(pageable)).thenReturn(pageVazia);

            Page<PrestadorResponseDTO> resultado = service.listarPrestadores(pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPrestadorPorId")
    class BuscarPrestadorPorId {

        @Test
        @DisplayName("Deve buscar prestador por ID com sucesso")
        void deveBuscarPrestadorPorIdComSucesso() {
            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            var resultado = service.buscarPrestadorPorId(prestadorId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(prestadorId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPrestadorPorId(prestadorId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));
        }
    }

    @Nested
    @DisplayName("atualizarPrestador")
    class AtualizarPrestador {

        @Test
        @DisplayName("Deve atualizar prestador com sucesso")
        void deveAtualizarPrestadorComSucesso() {
            var enderecoDTO = new EnderecoDTO("Rua Nova", "Centro", "01001000", "São Paulo", "SP", null, 200, null, null);
            var dto = new PrestadorRequestDTO("Novo Nome", "novo@teste.com", "Nova descrição",
                    "11888888888", enderecoDTO, categoriaId);

            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(categoriaRepository.findByIdAndAtivoTrue(categoriaId)).thenReturn(Optional.of(categoria));
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            var resultado = service.atualizarPrestador(dto, prestadorId);

            assertThat(resultado).isNotNull();
            verify(mapper).atualizarPrestadorDoDTO(dto, prestador);
            verify(repository).save(prestador);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado ao atualizar")
        void deveLancarExcecaoQuandoPrestadorNaoEncontradoAoAtualizar() {
            var dto = new PrestadorRequestDTO("Nome", "email@teste.com", "Desc",
                    "11999999999", null, categoriaId);

            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarPrestador(dto, prestadorId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando categoria não encontrada ao atualizar")
        void deveLancarExcecaoQuandoCategoriaNaoEncontradaAoAtualizar() {
            var dto = new PrestadorRequestDTO("Nome", "email@teste.com", "Desc",
                    "11999999999", null, categoriaId);

            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(categoriaRepository.findByIdAndAtivoTrue(categoriaId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarPrestador(dto, prestadorId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORIA_NAO_ENCONTRADA));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar prestador com sucesso")
        void deveDeletarPrestadorComSucesso() {
            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));

            service.deletar(prestadorId);

            assertThat(prestador.getAtivo()).isFalse();
            assertThat(prestador.getUsuario().getAtivo()).isFalse();
            verify(usuarioRepository).save(usuario);
            verify(repository).save(prestador);
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado ao deletar")
        void deveLancarExcecaoQuandoPrestadorNaoEncontradoAoDeletar() {
            when(repository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(prestadorId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPrestadoresProximos")
    class BuscarPrestadoresProximos {

        @Test
        @DisplayName("Deve buscar prestadores próximos com sucesso")
        void deveBuscarPrestadoresProximosComSucesso() {
            when(repository.buscarPorProximidade(-23.5, -46.6, 10.0)).thenReturn(List.of(prestador));
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            var resultado = service.buscarPrestadoresProximos(-23.5, -46.6, 10.0);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).nome()).isEqualTo("Prestador Teste");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há prestadores próximos")
        void deveRetornarListaVaziaQuandoNaoHaPrestadoresProximos() {
            when(repository.buscarPorProximidade(-23.5, -46.6, 10.0)).thenReturn(List.of());

            var resultado = service.buscarPrestadoresProximos(-23.5, -46.6, 10.0);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorFiltros")
    class BuscarPorFiltros {

        @Test
        @DisplayName("Deve buscar por filtros com raio informado")
        void deveBuscarPorFiltrosComRaioInformado() {
            when(repository.buscarPorCategoriaEProximidade(categoriaId, -23.5, -46.6, 20.0))
                    .thenReturn(List.of(prestador));
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            var resultado = service.buscarPorFiltros(categoriaId, -23.5, -46.6, 20.0);

            assertThat(resultado).hasSize(1);
            verify(repository).buscarPorCategoriaEProximidade(categoriaId, -23.5, -46.6, 20.0);
        }

        @Test
        @DisplayName("Deve usar raio padrão de 50km quando raio for nulo")
        void deveUsarRaioPadraoQuandoRaioForNulo() {
            when(repository.buscarPorCategoriaEProximidade(categoriaId, -23.5, -46.6, 50.0))
                    .thenReturn(List.of(prestador));
            when(mapper.toDto(prestador)).thenReturn(responseDTO);

            var resultado = service.buscarPorFiltros(categoriaId, -23.5, -46.6, null);

            assertThat(resultado).hasSize(1);
            verify(repository).buscarPorCategoriaEProximidade(categoriaId, -23.5, -46.6, 50.0);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não encontra prestadores")
        void deveRetornarListaVazia() {
            when(repository.buscarPorCategoriaEProximidade(categoriaId, -23.5, -46.6, 50.0))
                    .thenReturn(List.of());

            var resultado = service.buscarPorFiltros(categoriaId, -23.5, -46.6, null);

            assertThat(resultado).isEmpty();
        }
    }
}
