package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.dtos.autenticacao.DadosDeValidacaoDeCodigoRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRecuperacaoDTO;
import com.gabriel.party.dtos.autenticacao.DadosRedefinicaoDeSenhaDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.model.autenticacao.CodigoRecuperacao;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.autenticacao.CodigoRecuperacaoRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.cliente.ClienteService;
import com.gabriel.party.services.email.EmailService;
import com.gabriel.party.services.prestador.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PrestadorRepository prestadorRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    UsuarioMapper mapper;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    PrestadorService prestadorService;
    @Autowired
    ClienteService clienteService;
    @Autowired
    CodigoRecuperacaoRepository codigoRecuperacaoRepository;
    @Autowired
    EmailService emailService;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public CadastroResponseDTO cadastrarCliente(CadastroClienteDTO dto, MultipartFile fotoPerfil) {
        validarEmailExistente(dto.email());
        validarCpfOuCnpjExistente(dto.cpf());

        Usuario usuario = mapper.toUsuarioCliente(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_CLIENTE);

        clienteService.criarPerfilCliente(dto, usuario, fotoPerfil);

        String tokenJwt = tokenService.gerarToken(usuario);

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                tokenJwt);
    }

    public CadastroResponseDTO cadastrarPrestador(CadastroPrestadorDTO dto,MultipartFile fotoPerfil) {

        validarEmailExistente(dto.email());
        validarCpfOuCnpjExistente(dto.cnpjOuCpf());

        Usuario usuario = mapper.toUsuarioPrestador(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_PRESTADOR);

        prestadorService.criarPerfilPrestador(dto, usuario,fotoPerfil);

        String tokenJwt = tokenService.gerarToken(usuario);

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                tokenJwt);
    }


    private void validarEmailExistente(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USUARIO_JA_EXISTE_POR_EMAIL,
                    "Já existe um usuário cadastrado com esse email", email);
        }
    }

    private void validarCpfOuCnpjExistente(String cpfOuCnpj) {

        boolean cpfExiste = clienteRepository.existsByCpf(cpfOuCnpj);
        boolean cnpjExiste = prestadorRepository.existsByCnpjOuCpf(cpfOuCnpj);

        if (cpfExiste){
            throw new AppException(ErrorCode.JA_EXISTE_POR_CPF,
                    "Já existe um cadastro com esse CPF", cpfOuCnpj);
        }

        if (cnpjExiste){
            throw new AppException(ErrorCode.JA_EXISTE_POR_CNPJ,
                    "Já existe um cadastro com esse CNPJ", cpfOuCnpj);
        }

    }

    public void enviarCodigoRecuperacao(DadosRecuperacaoDTO dados) {

        var email = dados.email();

        usuarioRepository.findByEmail(email).ifPresent(user -> {

            String pin = String.format("%06d", new Random().nextInt(1000000));

            CodigoRecuperacao codigoRecuperacao = new CodigoRecuperacao();
            codigoRecuperacao.setCodigoPin(pin);
            codigoRecuperacao.setDataExpiracao(LocalDateTime.now().plusMinutes(15));
            codigoRecuperacao.setUsuario(user);
            codigoRecuperacaoRepository.save(codigoRecuperacao);

            String assunto = "Recuperação de Senha - FestConnect";
            String corpoEmail = String.format(
                    "Olá, %s!\n\n" +
                            "Recebemos uma solicitação para redefinir a sua senha.\n" +
                            "Use o código abaixo para prosseguir:\n\n" +
                            "CÓDIGO: %s\n\n" +
                            "Este código é válido por 15 minutos. Se não foi você quem solicitou, " +
                            "apenas ignore este e-mail por segurança.\n\n" +
                            "Atenciosamente,\nEquipe de Suporte.", pin
            );

            emailService.enviarEmail(user.getEmail(), assunto, corpoEmail);

        });
    }

    public String validarCodigoRecuperacao(DadosDeValidacaoDeCodigoRecuperacaoDTO dados) {

        var codigo = codigoRecuperacaoRepository.findByUsuarioEmail(dados.email())
                .orElseThrow(() -> new RuntimeException("Código não encontrado"));

        if (!codigo.getCodigoPin().equals(dados.codigoPin()) || codigo.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.CODIGO_INVALIDO_OU_EXPIRADO,
                    "O código de recuperação é inválido ou expirou", dados.email());
        }
        codigoRecuperacaoRepository.delete(codigo);

        return tokenService.gerarTokenParaRecuperacaoDeSenha(codigo.getUsuario());
    }

    @Transactional
    public void redefinirSenha(DadosRedefinicaoDeSenhaDTO dados){

        String email = tokenService.validarTokenDeRecuperacaoDeSenha(dados.token());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USUARIO_NAO_ENCONTRADO_POR_EMAIL,
                        "Usuário não encontrado para o email do token", email));

        usuario.setSenha(encoder.encode(dados.novaSenha()));
        usuarioRepository.save(usuario);
    }

}

