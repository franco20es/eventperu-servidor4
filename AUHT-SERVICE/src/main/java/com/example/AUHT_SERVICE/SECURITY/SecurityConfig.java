package com.example.AUHT_SERVICE.SECURITY;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // activar CORS con nuestra configuración personalizada
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

                .authorizeHttpRequests(auth -> auth
                        // ← rutas públicas (sin JWT)
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/otp",
                                "/api/v1/auth/2fa",
                                "/api/v1/auth/2fa/activar",
                                "/api/v1/auth/2fa/desactivar",
                                "/api/v1/auth/otp/activar",
                                "/api/v1/auth/otp/desactivar",
                                "/api/v1/auth/google/login",
                                "/api/v1/auth/google/register",
                                "/api/v1/auth/recuperar-password",
                                "/api/v1/auth/cambiar-password",
                                "/api/v1/auth/cambiar-password-perfil",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/qr",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/register-internal",
                                "/api/v1/auth/*/desactivar",  // ← mover aquí
                                "/api/v1/auth/*/reactivar"
                        ).permitAll()

                        // ← solo ROLE_ADMIN
                        .requestMatchers(
                                "/api/v1/auth/*/reactivar"
                        ).hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/v1/config/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )

            // jwtFilter se ejecuta antes que el filtro de autenticación de Spring Security
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
