package com.gabriel.party.config;

import com.gabriel.party.config.infra.security.SecurityFilter;
import com.gabriel.party.services.autenticacao.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public SecurityConfig(SecurityFilter securityFilter, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.securityFilter = securityFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/api/v1/auth/**").permitAll() // Permite acesso sem autenticação para rotas de autenticação
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Permite acesso sem autenticação para rotas de documentação

                        .requestMatchers(HttpMethod.GET, "/api/v1/prestadores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/itens-catalogo/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/avaliacoes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/eventos/tipos").permitAll()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**").hasAnyRole("CLIENTE", "ADMIN") // Apenas CLIENTE e ADMIN podem acessar
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**").hasAnyRole("CLIENTE", "ADMIN") // Apenas CLIENTE e ADMIN podem acessar
                        .requestMatchers(HttpMethod.PUT, "/api/v1/prestadores/**").hasAnyRole("PRESTADOR", "ADMIN") // Apenas PRESTADOR e ADMIN podem acessar
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/prestadores/**").hasAnyRole("PRESTADOR", "ADMIN") // Apenas PRESTADOR e ADMIN podem acessar

                        .requestMatchers("/api/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
                        .anyRequest().authenticated() // Qualquer outra rota exige token
                ).exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                ).oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/api/oauth2/authorization")
                        )
                        .failureHandler((req, res, ex) ->
                                res.sendRedirect(frontendUrl + "/login?erro=oauth")
                        )
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

        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}