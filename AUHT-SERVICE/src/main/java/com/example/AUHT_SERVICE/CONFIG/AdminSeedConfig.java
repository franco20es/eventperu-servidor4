package com.example.AUHT_SERVICE.CONFIG;


import com.example.AUHT_SERVICE.MODEL.ModelRoles;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@eventperu.com}")
    private String adminEmail;

    @Value("${admin.password:Admin2026secure}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedAdmin(RepositoryUsuario repo) {
        return args -> {
            if (repo.findByEmail(adminEmail).isEmpty()) {
                ModelUsuario admin = ModelUsuario.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .rol(ModelRoles.ROLE_ADMIN)
                        .activo(true)
                        .bloqueado(false)
                        .emailVerificado(true)
                        .intentosFallidos(0)
                        .otpActivado(false)
                        .twoFactorEnabled(false)
                        .build();
                repo.save(admin);
                log.warn("[AdminSeed] Admin creado: {}", adminEmail);
            } else {
                log.info("[AdminSeed] Admin ya existe: {}", adminEmail);
            }
        };
    }
}