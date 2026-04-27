package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.DadosDeValidacaoDeCodigoRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRedefinicaoDeSenhaDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.prestador.endereco.EnderecoDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.autenticacao.CodigoRecuperacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.cliente.ClienteService;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.prestador.PrestadorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrestadorRepository prestadorRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioMapper mapper;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private PrestadorService prestadorService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CodigoRecuperacaoRepository codigoRecuperacaoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AutenticacaoService service;

    private Usuario usuario;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEmail("teste@teste.com");
        usuario.setSenha("senhaEncriptada");
        usuario.setRole(Role.ROLE_CLIENTE);
        usuario.setAtivo(true);
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadUserByUsername {

        @Test
        @DisplayName("Deve carregar usuário por email com sucesso")
        void deveCarregarUsuarioPorEmailComSucesso() {
            when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));

            UserDetails resultado = service.loadUserByUsername("teste@teste.com");

            assertThat(resultado).isNotNull();
            assertThat(resultado.getUsername()).isEqualTo("teste@teste.com");
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("naoexiste@teste.com"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL));
        }
    }

    @Nested
    @DisplayName("cadastrarCliente")
    class CadastrarCliente {

        @Test
        @DisplayName("Deve cadastrar cliente com sucesso")
        void deveCadastrarClienteComSucesso() {
            var dto = new CadastroClienteDTO("João da Silva", "joao@teste.com", "senha123", null);

            when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(false);
            when(mapper.toUsuarioCliente(dto)).thenReturn(usuario);
            when(encoder.encode("senha123")).thenReturn("senhaEncriptada");
            when(usuarioRepository.save(usuario)).thenReturn(usuario);
            when(tokenService.gerarToken(usuario)).thenReturn("jwt-token");

            CadastroResponseDTO resultado = service.cadastrarCliente(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.token()).isEqualTo("jwt-token");
            assertThat(resultado.nomeCompleto()).isEqualTo("João da Silva");
            verify(usuarioRepository).save(usuario);
            assertThat(usuario.getRole()).isEqualTo(Role.ROLE_CLIENTE);
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            var dto = new CadastroClienteDTO("João", "existente@teste.com", "senha123", null);

            when(usuarioRepository.existsByEmail("existente@teste.com")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarCliente(dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USUARIO_JA_EXISTE_POR_EMAIL));

            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cadastrarPrestador")
    class CadastrarPrestador {

        @Test
        @DisplayName("Deve cadastrar prestador com sucesso")
        void deveCadastrarPrestadorComSucesso() {
            var enderecoDTO = new EnderecoDTO("Rua Teste", "Centro", "01001000", "São Paulo", "SP", null, 100, null, null);
            var dto = new CadastroPrestadorDTO("Empresa X", "empresa@teste.com", "senha123",
                    "11999999999", "12345678901234", UUID.randomUUID(), enderecoDTO);

            when(usuarioRepository.existsByEmail("empresa@teste.com")).thenReturn(false);
            when(prestadorRepository.existsByCnpjOuCpf("12345678901234")).thenReturn(false);
            when(mapper.toUsuarioPrestador(dto)).thenReturn(usuario);
            when(encoder.encode("senha123")).thenReturn("senhaEncriptada");
            when(prestadorService.criarPerfilPrestador(dto, usuario, null)).thenReturn(new Prestador());
            when(tokenService.gerarToken(usuario)).thenReturn("jwt-token");

            CadastroResponseDTO resultado = service.cadastrarPrestador(dto);

            assertThat(resultado).isNotNull();
            assertThat(resultado.token()).isEqualTo("jwt-token");
            assertThat(usuario.getRole()).isEqualTo(Role.ROLE_PRESTADOR);
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void deveLancarExcecaoQuandoEmailJaExiste() {
            var dto = new CadastroPrestadorDTO("Empresa", "existente@teste.com", "senha123",
                    "11999999999", "12345678901234", UUID.randomUUID(), null);

            when(usuarioRepository.existsByEmail("existente@teste.com")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarPrestador(dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USUARIO_JA_EXISTE_POR_EMAIL));

            verify(prestadorService, never()).criarPerfilPrestador(any(), any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CNPJ/CPF já existe")
        void deveLancarExcecaoQuandoCnpjJaExiste() {
            var dto = new CadastroPrestadorDTO("Empresa", "novo@teste.com", "senha123",
                    "11999999999", "12345678901234", UUID.randomUUID(), null);

            when(usuarioRepository.existsByEmail("novo@teste.com")).thenReturn(false);
            when(prestadorRepository.existsByCnpjOuCpf("12345678901234")).thenReturn(true);

            assertThatThrownBy(() -> service.cadastrarPrestador(dto))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.JA_EXISTE_POR_CNPJ));

            verify(prestadorService, never()).criarPerfilPrestador(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("enviarCodigoRecuperacao")
    class EnviarCodigoRecuperacao {

        @Test
        @DisplayName("Deve enviar código de recuperação quando usuário existir")
        void deveEnviarCodigoQuandoUsuarioExistir() {
            var dados = new DadosRecuperacaoDTO("teste@teste.com");

            when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));

            service.enviarCodigoRecuperacao(dados);

            verify(codigoRecuperacaoRepository).save(any(CodigoRecuperacao.class));
            verify(emailService).enviarEmail(eq("teste@teste.com"), anyString(), anyString());
        }

        @Test
        @DisplayName("Não deve fazer nada quando usuário não existir")
        void naoDeveFazerNadaQuandoUsuarioNaoExistir() {
            var dados = new DadosRecuperacaoDTO("naoexiste@teste.com");

            when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

            service.enviarCodigoRecuperacao(dados);

            verify(codigoRecuperacaoRepository, never()).save(any());
            verify(emailService, never()).enviarEmail(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("validarCodigoRecuperacao")
    class ValidarCodigoRecuperacao {

        @Test
        @DisplayName("Deve validar código e retornar token de recuperação")
        void deveValidarCodigoERetornarToken() {
            var dados = new DadosDeValidacaoDeCodigoRecuperacaoDTO("teste@teste.com", "123456");

            var codigo = new CodigoRecuperacao();
            codigo.setCodigoPin("123456");
            codigo.setDataExpiracao(LocalDateTime.now().plusMinutes(10));
            codigo.setUsuario(usuario);

            when(codigoRecuperacaoRepository.findByUsuarioEmail("teste@teste.com"))
                    .thenReturn(Optional.of(codigo));
            when(tokenService.gerarTokenParaRecuperacaoDeSenha(usuario)).thenReturn("recovery-token");

            String resultado = service.validarCodigoRecuperacao(dados);

            assertThat(resultado).isEqualTo("recovery-token");
            verify(codigoRecuperacaoRepository).delete(codigo);
        }

        @Test
        @DisplayName("Deve lançar exceção quando código não encontrado")
        void deveLancarExcecaoQuandoCodigoNaoEncontrado() {
            var dados = new DadosDeValidacaoDeCodigoRecuperacaoDTO("teste@teste.com", "123456");

            when(codigoRecuperacaoRepository.findByUsuarioEmail("teste@teste.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validarCodigoRecuperacao(dados))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando código incorreto")
        void deveLancarExcecaoQuandoCodigoIncorreto() {
            var dados = new DadosDeValidacaoDeCodigoRecuperacaoDTO("teste@teste.com", "000000");

            var codigo = new CodigoRecuperacao();
            codigo.setCodigoPin("123456");
            codigo.setDataExpiracao(LocalDateTime.now().plusMinutes(10));
            codigo.setUsuario(usuario);

            when(codigoRecuperacaoRepository.findByUsuarioEmail("teste@teste.com"))
                    .thenReturn(Optional.of(codigo));

            assertThatThrownBy(() -> service.validarCodigoRecuperacao(dados))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO));
        }

        @Test
        @DisplayName("Deve lançar exceção quando código expirado")
        void deveLancarExcecaoQuandoCodigoExpirado() {
            var dados = new DadosDeValidacaoDeCodigoRecuperacaoDTO("teste@teste.com", "123456");

            var codigo = new CodigoRecuperacao();
            codigo.setCodigoPin("123456");
            codigo.setDataExpiracao(LocalDateTime.now().minusMinutes(1)); // expirado
            codigo.setUsuario(usuario);

            when(codigoRecuperacaoRepository.findByUsuarioEmail("teste@teste.com"))
                    .thenReturn(Optional.of(codigo));

            assertThatThrownBy(() -> service.validarCodigoRecuperacao(dados))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO));
        }
    }

    @Nested
    @DisplayName("redefinirSenha")
    class RedefinirSenha {

        @Test
        @DisplayName("Deve redefinir senha com sucesso")
        void deveRedefinirSenhaComSucesso() {
            var dados = new DadosRedefinicaoDeSenhaDTO("recovery-token", "novaSenha123");

            when(tokenService.validarTokenDeRecuperacaoDeSenha("recovery-token"))
                    .thenReturn("teste@teste.com");
            when(usuarioRepository.findByEmail("teste@teste.com")).thenReturn(Optional.of(usuario));
            when(encoder.encode("novaSenha123")).thenReturn("novaSenhaEncriptada");

            service.redefinirSenha(dados);

            assertThat(usuario.getSenha()).isEqualTo("novaSenhaEncriptada");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não encontrado para redefinir senha")
        void deveLancarExcecaoQuandoUsuarioNaoEncontradoParaRedefinirSenha() {
            var dados = new DadosRedefinicaoDeSenhaDTO("recovery-token", "novaSenha123");

            when(tokenService.validarTokenDeRecuperacaoDeSenha("recovery-token"))
                    .thenReturn("naoexiste@teste.com");
            when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.redefinirSenha(dados))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL));

            verify(usuarioRepository, never()).save(any());
        }
    }
}
