package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Request.GoogleLoginRequest;
import com.example.AUHT_SERVICE.DTO.Response.GoogleLoginResponse;
import com.example.AUHT_SERVICE.DTO.Response.UserResponse;
import com.example.AUHT_SERVICE.EXCEPTION.DuplicateEmailException;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.EmailService;
import com.example.AUHT_SERVICE.SERVICE.GoogleAuthService;
import com.example.AUHT_SERVICE.SERVICE.OtpService;
import com.example.AUHT_SERVICE.UTILS.EmailTemplateBuilder;
import com.example.AUHT_SERVICE.UTILS.JwtUtil;
import com.example.AUHT_SERVICE.UTILS.OtpUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final RepositoryUsuario userRepository;
    private final com.example.AUHT_SERVICE.SECURITY.GoogleAuthService googleValidator;
    private final OtpUtil otpUtil;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserClient userClient;
    private final OtpService otpService;

    @Override
    @Transactional
    public GoogleLoginResponse loginWithGoogle(GoogleLoginRequest request) {
        log.info("Iniciando Login con Google");

        ModelUsuario user = googleValidator.validateAndCreateUser(request.getIdToken());
        String email = user.getEmail();

        // Si tiene OTP activado → enviar código y pedir verificación
        if (user.isOtpActivado()) {
            otpService.generateAndSendOtp(email);
            log.info("OTP enviado para Google login: {}", email);
            return GoogleLoginResponse.builder()
                    .email(email)
                    .rol(user.getRol().name())
                    .proveedor("google")
                    .otpRequired(true)
                    .build();
        }

        // Si tiene 2FA activado → pedir código de app
        if (user.isTwoFactorEnabled()) {
            log.info("2FA requerido para Google login: {}", email);
            return GoogleLoginResponse.builder()
                    .email(email)
                    .rol(user.getRol().name())
                    .proveedor("google")
                    .twoFactorRequired(true)
                    .build();
        }

        // Sin OTP ni 2FA → login directo con tokens
        return generarRespuestaCompleta(user);
    }

    @Override
    @Transactional
    public GoogleLoginResponse registerWithGoogle(GoogleLoginRequest request) {
        log.info("Iniciando registro con Google");

        ModelUsuario user = googleValidator.validateAndCreateUser(request.getIdToken());
        String email = user.getEmail();

        // Verificar si ya existe y tiene tokens (ya estaba registrado)
        if (userRepository.existsByEmail(email)) {
            // Si ya existe, tratarlo como login
            ModelUsuario existingUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (existingUser.isOtpActivado()) {
                otpService.generateAndSendOtp(email);
                return GoogleLoginResponse.builder()
                        .email(email)
                        .rol(existingUser.getRol().name())
                        .proveedor("google")
                        .otpRequired(true)
                        .build();
            }

            if (existingUser.isTwoFactorEnabled()) {
                return GoogleLoginResponse.builder()
                        .email(email)
                        .rol(existingUser.getRol().name())
                        .proveedor("google")
                        .twoFactorRequired(true)
                        .build();
            }

            return generarRespuestaCompleta(existingUser);
        }

        // Nuevo usuario → generar tokens directamente
        return generarRespuestaCompleta(user);
    }

    // ── Generar respuesta con tokens + userId ────────────────────
    private GoogleLoginResponse generarRespuestaCompleta(ModelUsuario user) {
        String email = user.getEmail();

        // Obtener userId del user-service
        String userServiceId = null;
        try {
            UserResponse userProfile = userClient.obtenerUsuarioPorEmail(email);
            if (userProfile != null && userProfile.getId() != null) {
                userServiceId = userProfile.getId().toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener userId del user-service: {}", e.getMessage());
        }

        // Generar tokens
        String accessToken = jwtUtil.generateToken(email, user.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        // Actualizar último login
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("Google login exitoso: {} rol={} userId={}", email, user.getRol().name(), userServiceId);

        return GoogleLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(email)
                .rol(user.getRol().name())
                .userId(userServiceId)
                .proveedor("google")
                .otpRequired(false)
                .twoFactorRequired(false)
                .build();
    }

    private void enviarEmailSeguridad(String email, String codigoOtp) {
        try {
            String htmlContent = EmailTemplateBuilder.buildOtpTemplate(
                    "SEGURIDAD",
                    "Usa el siguiente código de seguridad para acceder al sistema.",
                    codigoOtp
            );
            emailService.sendEmail(email, "Tu código de seguridad — EventPeru", htmlContent);
            log.info("Correo de seguridad enviado a {}", email);
        } catch (Exception e) {
            log.error("Fallo al enviar email de seguridad a {}: {}", email, e.getMessage());
        }
    }
}