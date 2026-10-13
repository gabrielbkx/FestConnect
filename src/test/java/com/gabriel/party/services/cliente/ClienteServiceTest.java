package com.gabriel.party.services.cliente;

import com.gabriel.party.dtos.cliente.ClienteRequestDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.mapper.categoria.CategoriaMapper;
import com.gabriel.party.mapper.cliente.ClienteMapper;
import com.gabriel.party.mapper.prestador.PrestadorMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.categoria.CategoriaRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.integracoes.aws.ArmazenamentoService;
import com.gabriel.party.services.integracoes.geocoding.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {
    // Um mock para cada dependência que existe de fato no ClienteService
    @Mock
    private ClienteRepository clienteRepository;
    @Mock private ClienteMapper mapper;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private GeocodingService geocodingService;
    @Mock private ArmazenamentoService armazenamentoService;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private PrestadorMapper prestadorMapper;
    @Mock private CategoriaMapper categoriaMapper;

    // O Mockito cria o service de verdade e passa os mocks acima no construtor
    @InjectMocks
    private ClienteService service;

    private Usuario usuarioA;
    private Usuario usuarioB;
    private Cliente clienteA;
    private ClienteRequestDTO dto;

    @BeforeEach
        // roda antes de CADA teste: cada teste começa com um cenário limpo
    void setUp() {
        usuarioA = criarUsuario(Role.ROLE_CLIENTE);
        usuarioB = criarUsuario(Role.ROLE_CLIENTE);

        clienteA = new Cliente();
        clienteA.setId(UUID.randomUUID());
        clienteA.setUsuario(usuarioA); // o perfil clienteA pertence ao usuarioA

        dto = new ClienteRequestDTO("Nome Novo", "novo@email.com", "11999999999", null);
    }

    private Usuario criarUsuario(Role role) {
        var usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setRole(role);
        return usuario;
    }

    @Test
    @DisplayName("cliente B não consegue atualizar o perfil do cliente A")
    void atualizar_outroCliente_lancaSemPermissao() {
        // Arrange: quando o service buscar o cliente A no "banco", devolve o objeto clienteA
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        // Act + Assert: o usuário B tenta alterar o perfil de A
        assertThatThrownBy(() -> service.atualizar(clienteA.getId(), dto, usuarioB))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USUARIO_SEM_PERMISSAO);

        // Assert: o dano NÃO aconteceu (nada foi alterado nem salvo)
        verify(mapper, never()).updateEntityFromDto(any(), any());
        verify(clienteRepository, never()).save(any());
    }
    @Test
    @DisplayName("o dono consegue atualizar o próprio perfil")
    void atualizar_dono_atualiza() {

        // Arrange
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        // Act
        service.atualizar(clienteA.getId(), dto, usuarioA);

        // Assert
        verify(mapper).updateEntityFromDto(dto, clienteA);
        verify(clienteRepository).save(clienteA);
    }

    @Test
    @DisplayName("o admin consegue atualizar o perfil de qualquer cliente")
    void atualizar_admin_atualiza() {
        // Arrange
        var admin = criarUsuario(Role.ROLE_ADMINISTRADOR);
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        // Act
        service.atualizar(clienteA.getId(), dto, admin);

        // Assert
        verify(mapper).updateEntityFromDto(dto, clienteA);
        verify(clienteRepository).save(clienteA);
    }

    // ── Demais operações: a regra já foi testada acima; aqui só provamos que cada
    //    método CHAMA a regra (um teste de ataque por método).

    @Test
    @DisplayName("cliente B não consegue excluir o perfil do cliente A")
    void deletar_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.deletar(clienteA.getId(), usuarioB));

        assertThat(clienteA.getAtivo()).isTrue();
        assertThat(usuarioA.getAtivo()).isTrue();
        verify(clienteRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue trocar a foto de perfil do cliente A")
    void atualizarFotoPerfil_outroCliente_lancaSemPermissao() {
        var arquivo = new MockMultipartFile("arquivo", "foto.png", "image/png", new byte[]{1});
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.atualizarFotoPerfil(clienteA.getId(), arquivo, usuarioB));

        verifyNoInteractions(armazenamentoService);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue adicionar prestador aos favoritos do cliente A")
    void adicionarPrestadorFavorito_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.adicionarPrestadorFavorito(clienteA.getId(), UUID.randomUUID(), usuarioB));

        verifyNoInteractions(prestadorRepository);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue remover prestador dos favoritos do cliente A")
    void removerPrestadorFavorito_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.removerPrestadorFavorito(clienteA.getId(), UUID.randomUUID(), usuarioB));

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue ver os prestadores favoritos do cliente A")
    void listarPrestadoresFavoritos_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.listarPrestadoresFavoritos(clienteA.getId(), usuarioB));

        verifyNoInteractions(prestadorMapper);
    }

    @Test
    @DisplayName("cliente B não consegue adicionar categoria aos favoritos do cliente A")
    void adicionarCategoriaFavorita_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.adicionarCategoriaFavorita(clienteA.getId(), UUID.randomUUID(), usuarioB));

        verifyNoInteractions(categoriaRepository);
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue remover categoria dos favoritos do cliente A")
    void removerCategoriaFavorita_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.removerCategoriaFavorita(clienteA.getId(), UUID.randomUUID(), usuarioB));

        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("cliente B não consegue ver as categorias favoritas do cliente A")
    void listarCategoriasFavoritas_outroCliente_lancaSemPermissao() {
        when(clienteRepository.findByIdAndAtivoTrue(clienteA.getId())).thenReturn(Optional.of(clienteA));

        assertSemPermissao(() -> service.listarCategoriasFavoritas(clienteA.getId(), usuarioB));

        verifyNoInteractions(categoriaMapper);
    }

    private void assertSemPermissao(ThrowingCallable acao) {
        assertThatThrownBy(acao)
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USUARIO_SEM_PERMISSAO);
    }
}

