package com.gabriel.party.services.avaliacao;

import com.gabriel.party.dtos.avaliacao.AvaliacaoCreateDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoRequestDTO;
import com.gabriel.party.dtos.avaliacao.AvaliacaoResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.avaliacao.AvaliacaoMapper;
import com.gabriel.party.model.avaliacao.Avaliacao;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.avaliacao.AvaliacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository repository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private AvaliacaoMapper mapper;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private AvaliacaoService service;

    private UUID usuarioId;
    private UUID prestadorId;
    private UUID clienteId;
    private UUID avaliacaoId;
    private Cliente cliente;
    private Prestador prestador;
    private Avaliacao avaliacao;
    private AvaliacaoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        prestadorId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        avaliacaoId = UUID.randomUUID();

        cliente = new Cliente();
        cliente.setId(clienteId);

        prestador = new Prestador();
        prestador.setId(prestadorId);

        avaliacao = new Avaliacao();
        avaliacao.setId(avaliacaoId);
        avaliacao.setNota(5);
        avaliacao.setComentario("Excelente");
        avaliacao.setCliente(cliente);
        avaliacao.setPrestador(prestador);
        avaliacao.setAtivo(true);

        responseDTO = new AvaliacaoResponseDTO(
                avaliacaoId, 5, "Excelente", LocalDateTime.now(),
                prestadorId, "Prestador Teste", clienteId, "Cliente Teste"
        );
    }

    @Nested
    @DisplayName("salvarAvaliacao")
    class SalvarAvaliacao {

        @Test
        @DisplayName("Deve salvar avaliação com sucesso")
        void deveSalvarAvaliacaoComSucesso() {
            var dto = new AvaliacaoCreateDTO(5, "Excelente", prestadorId);

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.of(prestador));
            when(mapper.toEntity(dto)).thenReturn(avaliacao);
            when(mapper.toDto(avaliacao)).thenReturn(responseDTO);

            var resultado = service.salvarAvaliacao(dto, usuarioId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.nota()).isEqualTo(5);
            verify(repository).save(avaliacao);
            verify(mapper).toEntity(dto);
            verify(mapper).toDto(avaliacao);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            var dto = new AvaliacaoCreateDTO(5, "Excelente", prestadorId);

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.salvarAvaliacao(dto, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CLIENTE_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prestador não encontrado")
        void deveLancarExcecaoQuandoPrestadorNaoEncontrado() {
            var dto = new AvaliacaoCreateDTO(5, "Excelente", prestadorId);

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
            when(prestadorRepository.findByIdAndAtivoTrue(prestadorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.salvarAvaliacao(dto, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PRESTADOR_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarAvaliacoes")
    class ListarAvaliacoes {

        @Test
        @DisplayName("Deve listar avaliações com paginação")
        void deveListarAvaliacoesComPaginacao() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Avaliacao> page = new PageImpl<>(List.of(avaliacao));

            when(repository.findAllByAtivoTrue(pageable)).thenReturn(page);
            when(mapper.toDto(avaliacao)).thenReturn(responseDTO);

            Page<AvaliacaoResponseDTO> resultado = service.listarAvaliacoes(pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).nota()).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há avaliações")
        void deveRetornarPaginaVaziaQuandoNaoHaAvaliacoes() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Avaliacao> pageVazia = new PageImpl<>(List.of());

            when(repository.findAllByAtivoTrue(pageable)).thenReturn(pageVazia);

            Page<AvaliacaoResponseDTO> resultado = service.listarAvaliacoes(pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarAvaliacaoPorId")
    class BuscarAvaliacaoPorId {

        @Test
        @DisplayName("Deve buscar avaliação por ID com sucesso")
        void deveBuscarAvaliacaoPorIdComSucesso() {
            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));
            when(mapper.toDto(avaliacao)).thenReturn(responseDTO);

            var resultado = service.buscarAvaliacaoPorId(avaliacaoId);

            assertThat(resultado).isNotNull();
            assertThat(resultado.id()).isEqualTo(avaliacaoId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando avaliação não encontrada")
        void deveLancarExcecaoQuandoAvaliacaoNaoEncontrada() {
            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarAvaliacaoPorId(avaliacaoId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AVALIACAO_NAO_ENCONTRADA));
        }
    }

    @Nested
    @DisplayName("atualizarAvaliacao")
    class AtualizarAvaliacao {

        @Test
        @DisplayName("Deve atualizar avaliação com sucesso")
        void deveAtualizarAvaliacaoComSucesso() {
            var dto = new AvaliacaoRequestDTO(4, "Bom");

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));
            when(mapper.toDto(avaliacao)).thenReturn(responseDTO);

            var resultado = service.atualizarAvaliacao(dto, avaliacaoId, usuarioId);

            assertThat(resultado).isNotNull();
            verify(mapper).atualizarAvaliacaoDoDTO(dto, avaliacao);
            verify(repository).save(avaliacao);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado ao atualizar")
        void deveLancarExcecaoQuandoClienteNaoEncontradoAoAtualizar() {
            var dto = new AvaliacaoRequestDTO(4, "Bom");

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarAvaliacao(dto, avaliacaoId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CLIENTE_NAO_ENCONTRADO));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando avaliação não encontrada ao atualizar")
        void deveLancarExcecaoQuandoAvaliacaoNaoEncontradaAoAtualizar() {
            var dto = new AvaliacaoRequestDTO(4, "Bom");

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizarAvaliacao(dto, avaliacaoId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AVALIACAO_NAO_ENCONTRADA));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando avaliação não pertence ao cliente")
        void deveLancarExcecaoQuandoAvaliacaoNaoPertenceAoCliente() {
            var dto = new AvaliacaoRequestDTO(4, "Bom");

            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());

            avaliacao.setCliente(outroCliente);

            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));
            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));

            assertThatThrownBy(() -> service.atualizarAvaliacao(dto, avaliacaoId, usuarioId))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AVALIACAO_NAO_PERTENCE_AO_CLIENTE_MENCIONADO));

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar avaliação como administrador")
        void deveDeletarAvaliacaoComoAdministrador() {
            var usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setRole(Role.ROLE_ADMINISTRADOR);

            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));

            service.deletar(avaliacaoId, usuario);

            assertThat(avaliacao.getAtivo()).isFalse();
            verify(repository).save(avaliacao);
            verify(clienteRepository, never()).findByUsuarioIdAndAtivoTrue(any());
        }

        @Test
        @DisplayName("Deve deletar avaliação como cliente dono")
        void deveDeletarAvaliacaoComoClienteDono() {
            var usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setRole(Role.ROLE_CLIENTE);

            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));
            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));

            service.deletar(avaliacaoId, usuario);

            assertThat(avaliacao.getAtivo()).isFalse();
            verify(repository).save(avaliacao);
        }

        @Test
        @DisplayName("Deve lançar exceção quando avaliação não encontrada ao deletar")
        void deveLancarExcecaoQuandoAvaliacaoNaoEncontradaAoDeletar() {
            var usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setRole(Role.ROLE_CLIENTE);

            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(avaliacaoId, usuario))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AVALIACAO_NAO_ENCONTRADA));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado ao deletar")
        void deveLancarExcecaoQuandoClienteNaoEncontradoAoDeletar() {
            var usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setRole(Role.ROLE_CLIENTE);

            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));
            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(avaliacaoId, usuario))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CLIENTE_NAO_ENCONTRADO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente tenta deletar avaliação de outro cliente")
        void deveLancarExcecaoQuandoClienteTentaDeletarAvaliacaoDeOutro() {
            var usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setRole(Role.ROLE_CLIENTE);

            var outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());
            avaliacao.setCliente(outroCliente);

            when(repository.findByIdAndAtivoTrue(avaliacaoId)).thenReturn(Optional.of(avaliacao));
            when(clienteRepository.findByUsuarioIdAndAtivoTrue(usuarioId)).thenReturn(Optional.of(cliente));

            assertThatThrownBy(() -> service.deletar(avaliacaoId, usuario))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.AVALIACAO_NAO_PERTENCE_AO_CLIENTE_MENCIONADO));
        }
    }
}
