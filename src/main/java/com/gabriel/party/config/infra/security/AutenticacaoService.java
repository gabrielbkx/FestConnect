package com.gabriel.party.config.infra.security;

import com.gabriel.party.dtos.autenticacao.cadastro.CadastroRequestDTO;
import com.gabriel.party.dtos.autenticacao.cadastro.CadastroResponseDTO;
import com.gabriel.party.dtos.autenticacao.login.LoginRequestDTO;
import com.gabriel.party.dtos.autenticacao.login.TokenResponseDTO;
import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.mapper.autenticacao.AutenticacaoMapper;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    TokenService tokenService;
    @Autowired
    AutenticacaoMapper mapper;
    @Autowired
    PasswordEncoder encoder;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }


    @Transactional
    public CadastroResponseDTO cadastrarUsuario(@Valid CadastroRequestDTO dto) {

        boolean usuarioJaexistePorEmail = usuarioRepository.existsByEmail(dto.email());

        if (usuarioJaexistePorEmail){
            throw new AppException(ErrorCode.USUARIO_JA_EXISTE_POR_EMAIL,
                    "Já existe um usuário cadastrado com esse email", dto.email());
        }

        var usuario = mapper.toEntity(dto);
        usuario.setSenha(encoder.encode(dto.senha()));

        usuarioRepository.save(usuario);

        String token = tokenService.gerarToken(usuario);

        return new CadastroResponseDTO(
                usuario.getId(),
                dto.nomeCompleto(),
                usuario.getEmail(),
                token,
                new TokenResponseDTO(token)
        );
    }
}
