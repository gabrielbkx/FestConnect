package com.gabriel.party.config;

import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o administrador inicial a partir das variáveis ADMIN_EMAIL e ADMIN_SENHA.
 * <ul>
 *   <li>Variáveis ausentes: não cria nenhum admin (só avisa no log). A aplicação sobe normalmente,
 *       o que permite remover o segredo do ambiente depois que o admin já existe.</li>
 *   <li>Senha curta: a aplicação NÃO sobe. Alguém tentou configurar e configurou errado; aceitar
 *       uma senha fraca em silêncio é exatamente o problema que esta classe evita.</li>
 *   <li>Admin já existe: não altera nada (a senha não é sobrescrita a cada subida).</li>
 * </ul>
 */
@Slf4j
@Component
public class CriarAdmin implements CommandLineRunner {

    static final int TAMANHO_MINIMO_SENHA = 12;

    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    private final String email;

    private final String senha;

    public CriarAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      @Value("${festconnect.admin.email:}") String email,
                      @Value("${festconnect.admin.senha:}") String senha) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {

        if (email.isBlank() || senha.isBlank()) {
            log.warn("CriarAdmin: ADMIN_EMAIL/ADMIN_SENHA não definidos, nenhum admin foi criado.");
            return;
        }

        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalStateException(
                    "ADMIN_SENHA precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }

        if (usuarioRepository.existsByEmail(email)) {
            return;
        }

        var usuarioAdmin = new Usuario();
        usuarioAdmin.setSenha(passwordEncoder.encode(senha));
        usuarioAdmin.setEmail(email);
        usuarioAdmin.setRole(Role.ROLE_ADMINISTRADOR);
        usuarioRepository.save(usuarioAdmin);
        log.info("CriarAdmin: administrador {} criado.", email);
    }

}
