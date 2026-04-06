package com.gabriel.party.config.infra.security;

import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.cliente.CadastroClienteDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.prestador.CadastroPrestadorDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.UsuarioMapper;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.prestador.Prestador;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import com.gabriel.party.services.cliente.ClienteService;
import com.gabriel.party.services.prestador.PrestadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }


    @Transactional
    public CadastroResponseDTO cadastrarCliente(CadastroClienteDTO dto) {
        validarEmailExistente(dto.email());
        validarCpfOuCnpjExistente(dto.cpf());

        Usuario usuario = mapper.toUsuarioCliente(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_CLIENTE);

        Cliente clienteSalvo = clienteService.criarPerfilCliente(dto, usuario);
        String tokenJwt = tokenService.gerarToken(usuario);

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                tokenJwt);
    }


    public CadastroResponseDTO cadastrarPrestador(CadastroPrestadorDTO dto) {

        validarEmailExistente(dto.email());
        validarCpfOuCnpjExistente(dto.cnpjOuCpf());

        Usuario usuario = mapper.toUsuarioPrestador(dto);
        usuario.setSenha(encoder.encode(dto.senha()));
        usuario.setRole(Role.ROLE_PRESTADOR);

        Prestador prestadorSalvo = prestadorService.criarPerfilPrestador(dto, usuario);

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
}
