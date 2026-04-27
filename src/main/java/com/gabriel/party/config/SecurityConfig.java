package com.gabriel.party.config;

import com.gabriel.party.config.infra.security.SecurityFilter;
import com.gabriel.party.services.autenticacao.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(SecurityFilter securityFilter, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.securityFilter = securityFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .cors(Customizer.withDefaults())
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
                ).oauth2Login(Customizer.withDefaults())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                ).oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Coloque aqui a porta onde o seu React está rodando
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // Libera os métodos e os cabeçalhos que o Axios vai usar
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica para a API toda

        return source;
    }
}