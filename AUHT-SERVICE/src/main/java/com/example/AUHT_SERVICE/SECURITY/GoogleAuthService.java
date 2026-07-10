package com.example.AUHT_SERVICE.SECURITY;

import com.example.AUHT_SERVICE.MODEL.ModelRoles;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.client.UserClient;
import com.example.AUHT_SERVICE.DTO.Request.UserRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthService {

    private final RepositoryUsuario userRepository;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;

    @Transactional
    public ModelUsuario validateAndCreateUser(String idToken) {
        try {
            log.info("Procesando autenticación de Google...");

            String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Formato de token Google inválido");
            }

            String decodedPayload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode claims = objectMapper.readTree(decodedPayload);

            String email = claims.path("email").asText();
            String nombre = claims.path("given_name").asText("");
            String apellido = claims.path("family_name").asText("");
            String fotoPerfil = claims.path("picture").asText("");

            if (email == null || email.isEmpty()) {
                throw new RuntimeException("El token de Google no contiene un email válido");
            }

            return userRepository.findByEmail(email)
                    .map(existingUser -> updateGoogleUser(existingUser))
                    .orElseGet(() -> createGoogleUser(email, nombre, apellido, fotoPerfil));

        } catch (Exception e) {
            log.error("Error en Google Auth: {}", e.getMessage());
            throw new RuntimeException("Fallo en la autenticación con Google");
        }
    }

    private ModelUsuario updateGoogleUser(ModelUsuario user) {
        log.info("Usuario Google existente: {}", user.getEmail());
        user.setEmailVerificado(true);
        return userRepository.save(user);
    }

    private ModelUsuario createGoogleUser(String email, String nombre, String apellido, String fotoPerfil) {
        log.info("Registrando nuevo usuario vía Google: {}", email);

        // 1. Crear en auth-service
        ModelUsuario newUser = ModelUsuario.builder()
                .email(email)
                .password("")
                .rol(ModelRoles.ROLE_USER)
                .activo(true)
                .emailVerificado(true)
                .bloqueado(false)
                .intentosFallidos(0)
                .twoFactorEnabled(false)
                .build();
        ModelUsuario saved = userRepository.save(newUser);

        // 2. Crear perfil en user-service
        try {
            userClient.createUser(UserRequest.builder()
                    .email(email)
                    .nombre(nombre.isEmpty() ? email.split("@")[0] : nombre)
                    .apellido(apellido.isEmpty() ? "" : apellido)
                    .dni("00000000")
                    .telefono("000000000")
                    .build());
            log.info("Perfil creado en user-service para: {}", email);
        } catch (Exception e) {
            log.warn("No se pudo crear perfil en user-service: {}", e.getMessage());
            // No hacer rollback — el usuario puede completar su perfil después
        }

        return saved;
    }
}