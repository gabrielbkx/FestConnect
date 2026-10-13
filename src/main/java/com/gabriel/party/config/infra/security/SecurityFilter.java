package com.gabriel.party.config.infra.security;

import com.gabriel.party.exceptions.AppException;
import com.gabriel.party.model.usuario.Usuario;
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
import java.time.Instant;

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
            var tokenDeLogin = tokenService.validarTokenDeLogin(token);

            // Só o campo "ativo" decide. Não usamos isEnabled(): ele também exige e-mail verificado,
            // e o token entregue no cadastro (antes da verificação) pararia de funcionar.
            usuarioRepository.findByEmail(tokenDeLogin.email())
                    .filter(usuario -> Boolean.TRUE.equals(usuario.getAtivo()))
                    .filter(usuario -> sessaoAindaValida(usuario, tokenDeLogin))
                    .ifPresentOrElse(usuario -> {
                        var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }, () -> {
                        log.debug("Token de usuário inexistente, desativado ou de sessão encerrada; requisição segue anônima.");
                        SecurityContextHolder.clearContext();
                    });

        } catch (AppException e) {
            // Token vencido é normal (o front manda o token em toda chamada), por isso debug e não erro
            log.debug("Token recusado ({}); requisição segue anônima.", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * "Sair de todos os dispositivos" e troca de senha gravam usuario.sessoesValidasDesde; todo token
     * emitido antes disso deixa de valer. Custo zero: o usuário já é buscado no banco acima.
     */
    private static boolean sessaoAindaValida(Usuario usuario, TokenDeLogin token) {
        Instant validasDesde = usuario.getSessoesValidasDesde();
        if (validasDesde == null) {
            return true;
        }
        // Token sem data de emissão (anterior a esta versão): não dá para provar que é novo, então não vale
        return token.emitidoEm() != null && !token.emitidoEm().isBefore(validasDesde);
    }

    private String recuperarToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;

    }
}
