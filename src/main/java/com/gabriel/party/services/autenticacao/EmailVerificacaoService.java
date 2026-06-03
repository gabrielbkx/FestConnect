package com.gabriel.party.services.autenticacao;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.exceptions.enums.ErrorCode;
import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import com.gabriel.party.services.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailVerificacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public void enviarVerificacaoEmail(Usuario u) {
        String token = UUID.randomUUID().toString();
        u.setTokenVerificacao(token);
        u.setTokenExpiracao(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(u);

        String link = frontendUrl + "/verificar-email?token=" + token;
        String corpo = "<h2>Bem-vindo ao FestConnect!</h2>"
                + "<p>Confirme seu endereço de e-mail clicando no link abaixo:</p>"
                + "<p><a href=\"" + link + "\">Verificar e-mail</a></p>"
                + "<p>Este link expira em 24 horas.</p>";
        emailService.enviarEmail(u.getEmail(), "Verifique seu e-mail — FestConnect", corpo);
    }

    public void verificarToken(String token) {
        Usuario usuario = usuarioRepository.findByTokenVerificacao(token)
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_DE_VERIFICACAO_NAO_ENCONTRADO, token));

        if (usuario.getTokenExpiracao().isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.TOKEN_DE_VERIFICACAO_EXPIRADO, token);

        usuario.setEmailVerificado(true);
        usuario.setTokenVerificacao(null);
        usuario.setTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }
}