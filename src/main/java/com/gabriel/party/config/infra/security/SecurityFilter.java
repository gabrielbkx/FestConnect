package com.gabriel.party.config.infra.security;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.repositories.Usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lê o JWT da requisição e, se ele for válido, coloca o usuário no contexto do Spring Security.
 * <p>
 * O filtro só AUTENTICA; ele não decide o que é proibido. Se o token não serve (inválido, vencido,
 * usuário inexistente ou desativado), a requisição segue como anônima, e quem decide é a SecurityConfig:
 * rota pública responde normalmente, rota protegida devolve 401 pelo authenticationEntryPoint.
 * Lançar exceção aqui não adianta: o filtro roda antes do DispatcherServlet, então o
 * GlobalExceptionHandler nunca a veria.
 */
@Slf4j
@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;
    @Autowired
    UsuarioRepository usuarioRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var token = recuperarToken(request);

        if (token != null) {
            autenticar(token);
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(String token) {
        try {
            var email = tokenService.validarTokenDeLogin(token);

            // Só o campo "ativo" decide. Não usamos isEnabled(): ele também exige e-mail verificado,
            // e o token entregue no cadastro (antes da verificação) pararia de funcionar.
            usuarioRepository.findByEmail(email)
                    .filter(usuario -> Boolean.TRUE.equals(usuario.getAtivo()))
                    .ifPresentOrElse(usuario -> {
                        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }, () -> {
                        log.debug("Token de usuário inexistente ou desativado; requisição segue anônima.");
                        SecurityContextHolder.clearContext();
                    });

        } catch (AppException e) {
            // Token vencido é normal (o front manda o token em toda chamada), por isso debug e não erro
            log.debug("Token recusado ({}); requisição segue anônima.", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    private String recuperarToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;

    }
}
