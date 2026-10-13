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
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
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
}

