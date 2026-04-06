package com.gabriel.party.config;

import com.gabriel.party.config.infra.security.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/auth/**").permitAll() // Permite acesso sem autenticação para rotas de autenticação
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Permite acesso sem autenticação para rotas de documentação

                        .requestMatchers(HttpMethod.GET, "/prestadores/**").permitAll() // Permite acesso público para GET em prestadores

                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasAnyRole("CLIENTE", "ADMIN") // Apenas CLIENTE e ADMIN podem acessar
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasAnyRole("CLIENTE", "ADMIN") // Apenas CLIENTE e ADMIN podem acessar
                        .requestMatchers(HttpMethod.PUT, "/prestadores/**").hasAnyRole("PRESTADOR", "ADMIN") // Apenas PRESTADOR e ADMIN podem acessar
                        .requestMatchers(HttpMethod.DELETE, "/prestadores/**").hasAnyRole("PRESTADOR", "ADMIN") // Apenas PRESTADOR e ADMIN podem acessar

                        .anyRequest().authenticated() // Qualquer outra rota exige token
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
