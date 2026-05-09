package com.gabriel.party.config;

import com.gabriel.party.model.usuario.Usuario;
import com.gabriel.party.model.usuario.enums.Role;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CriarAdmin implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    private final UsuarioRepository usuarioRepository;

    public CriarAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        boolean usuarioExiste = usuarioRepository.existsByEmail("admin@admin.com");

        if (usuarioExiste) {
            return;
        }

        var usuarioAdmin = new Usuario();
        usuarioAdmin.setSenha(passwordEncoder.encode("admin"));
        usuarioAdmin.setEmail("admin@admin.com");
        usuarioAdmin.setRole(Role.ROLE_ADMINISTRADOR);
        usuarioRepository.save(usuarioAdmin);
    }

}
