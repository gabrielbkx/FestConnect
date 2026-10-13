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
import jakarta.servlet.http.HttpSession;
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
            // Conta desativada não recebe token. Aqui dá para dizer o motivo: quem chegou neste
            // ponto já provou ao Google que é dono do e-mail.
            if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                encerrarSessaoDoLogin(request);
                response.sendRedirect(frontendUrl + "/login?erro=conta_desativada");
                return;
            }
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
        String fotoPerfil = null;
        if (usuario.getRole() == Role.ROLE_CLIENTE) {
            var cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (cliente != null) { profileId = cliente.getId(); nomeCompleto = cliente.getNomeCompleto(); fotoPerfil = cliente.getFotoPerfilUrl(); }
        } else if (usuario.getRole() == Role.ROLE_PRESTADOR) {
            var prestador = prestadorRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (prestador != null) { profileId = prestador.getId(); nomeCompleto = prestador.getNomeCompleto(); fotoPerfil = prestador.getFotoPerfilUrl(); }
        }
        String tokenJwt = tokenService.gerarToken(usuario, profileId, nomeCompleto, fotoPerfil);
        encerrarSessaoDoLogin(request);

        // Token no FRAGMENTO (#), não na query (?): o fragmento não é enviado ao servidor, então não
        // aparece em logs de acesso (Vercel, proxies) nem vai no cabeçalho Referer.
        response.sendRedirect(frontendUrl + "/oauth2/callback#token=" + tokenJwt);
    }

    /**
     * A sessão HTTP só existe para guardar o estado do fluxo OAuth2 com o Google. Terminado o login,
     * a API passa a usar o JWT; deixar a sessão viva manteria um segundo jeito de estar autenticado
     * (pelo cookie JSESSIONID) sem necessidade.
     */
    private void encerrarSessaoDoLogin(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate();
        }
    }
}

