package com.gabriel.party.services.autenticacao;

import com.gabriel.party.config.infra.security.TokenService;
import com.gabriel.party.model.cliente.Cliente;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.repositories.cliente.ClienteRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.gabriel.party.repositories.prestador.PrestadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PrestadorRepository prestadorRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String nome = oAuth2User.getAttribute("name");
        String fotoPerfilUrl = oAuth2User.getAttribute("picture");

        // 1. Buscamos o usuário no banco
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        Usuario usuario;

        if (usuarioOpt.isPresent()) {
            usuario = usuarioOpt.get();
        } else {
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setRole(Role.ROLE_CLIENTE);

            String senhaAleatoria = UUID.randomUUID().toString();
            usuario.setSenha(passwordEncoder.encode(senhaAleatoria));
            usuario.setEmailVerificado(true);

            Cliente cliente = new Cliente();
            cliente.setNomeCompleto(nome);
            cliente.setFotoPerfilUrl(fotoPerfilUrl);

            cliente.setUsuario(usuario);


            clienteRepository.save(cliente);
        }

        UUID profileId = null;
        String nomeCompleto = null;
        if (usuario.getRole() == Role.ROLE_CLIENTE) {
            var cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (cliente != null) { profileId = cliente.getId(); nomeCompleto = cliente.getNomeCompleto(); }
        } else if (usuario.getRole() == Role.ROLE_PRESTADOR) {
            var prestador = prestadorRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (prestador != null) { profileId = prestador.getId(); nomeCompleto = prestador.getNomeCompleto(); }
        }
        String tokenJwt = tokenService.gerarToken(usuario, profileId, nomeCompleto);
        response.sendRedirect(frontendUrl + "/oauth2/callback?token=" + tokenJwt);
    }
}

