package com.gabriel.party.services.prestador;

import com.gabriel.party.dtos.prestador.PrestadorRequestDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.pedido.PedidoRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestadorServiceTest {

    @Mock private PrestadorRepository repository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private PrestadorMapper mapper;
    @Mock private GeocodingService geocodingService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ArmazenamentoService armazenamentoService;
    @Mock private PedidoRepository pedidoRepository;

    @InjectMocks private PrestadorService service;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Prestador prestadorA;
    private PrestadorRequestDTO dto;

    @BeforeEach
    void setUp() {
        usuarioA = criarUsuario(Role.ROLE_PRESTADOR);
        usuarioB = criarUsuario(Role.ROLE_PRESTADOR);

        prestadorA = new Prestador();
        prestadorA.setId(UUID.randomUUID());
        prestadorA.setUsuario(usuarioA); // o perfil prestadorA pertence ao usuarioA

        // endereco null: o service não chama o geocoding
        dto = new PrestadorRequestDTO("Buffet Novo", "novo@buffet.com", "Descrição", "11999999999", null);
    }

    private Usuario criarUsuario(Role role) {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRole(role);
        return usuario;
    }

    // ── A regra (dono ou admin), testada uma vez pelo atualizar

    @Test
    @DisplayName("prestador B não consegue atualizar o perfil do prestador A")
    void atualizarPrestador_outroPrestador_lancaSemPermissao() {
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        assertSemPermissao(() -> service.atualizarPrestador(dto, prestadorA.getId(), usuarioB));

        verify(mapper, never()).atualizarPrestadorDoDTO(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("o dono consegue atualizar o próprio perfil de prestador")
    void atualizarPrestador_dono_atualiza() {
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        service.atualizarPrestador(dto, prestadorA.getId(), usuarioA);

        verify(mapper).atualizarPrestadorDoDTO(dto, prestadorA);
        verify(repository).save(prestadorA);
    }

    @Test
    @DisplayName("o admin consegue atualizar o perfil de qualquer prestador")
    void atualizarPrestador_admin_atualiza() {
        var admin = criarUsuario(Role.ROLE_ADMINISTRADOR);
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        service.atualizarPrestador(dto, prestadorA.getId(), admin);

        verify(mapper).atualizarPrestadorDoDTO(dto, prestadorA);
        verify(repository).save(prestadorA);
    }

    // ── Demais operações: um teste de ataque por método prova que cada um CHAMA a regra

    @Test
    @DisplayName("prestador B não consegue excluir o perfil do prestador A")
    void deletar_outroPrestador_lancaSemPermissao() {
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        assertSemPermissao(() -> service.deletar(prestadorA.getId(), usuarioB));

        assertThat(prestadorA.getAtivo()).isTrue();
        assertThat(usuarioA.getAtivo()).isTrue();
        verify(repository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("prestador B não consegue trocar a foto de perfil do prestador A")
    void atualizarFotoPerfil_outroPrestador_lancaSemPermissao() {
        var arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", new byte[]{1});
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        assertSemPermissao(() -> service.atualizarFotoPerfil(prestadorA.getId(), arquivo, usuarioB));

        verifyNoInteractions(armazenamentoService);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("prestador B não consegue trocar a foto de capa do prestador A")
    void atualizarFotoBanner_outroPrestador_lancaSemPermissao() {
        var arquivo = new MockMultipartFile("arquivo", "capa.png", "image/png", new byte[]{1});
        when(repository.findByIdAndAtivoTrue(prestadorA.getId())).thenReturn(Optional.of(prestadorA));

        assertSemPermissao(() -> service.atualizarFotoBanner(prestadorA.getId(), arquivo, usuarioB));

        verifyNoInteractions(armazenamentoService);
        verify(repository, never()).save(any());
    }

    private void assertSemPermissao(ThrowingCallable acao) {
        assertThatThrownBy(acao)
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USUARIO_SEM_PERMISSAO);
    }
}
