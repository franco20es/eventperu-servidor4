package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import java.time.LocalDateTime;
import java.util.Map;


import com.example.AUHT_SERVICE.DTO.Request.InternalRegisterRequest;
import com.example.AUHT_SERVICE.DTO.Request.LoginRequest;
import com.example.AUHT_SERVICE.DTO.Request.RegisterRequest;
import com.example.AUHT_SERVICE.EXCEPTION.EmailAlreadyExistsException;
import com.example.AUHT_SERVICE.EXCEPTION.InvalidPasswordException;
import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import com.example.AUHT_SERVICE.SERVICE.*;
import com.example.AUHT_SERVICE.UTILS.PasswordSeguroUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import com.example.AUHT_SERVICE.DTO.Response.UserResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.MODEL.ModelRoles;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.UTILS.JwtUtil;
import com.example.AUHT_SERVICE.DTO.Request.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RepositoryUsuario userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;
    private final OtpService        otpService;
    private final UserClient        userClient;
    private final AuditService      auditService;  // ← inyectado

    private static final String SERVICIO = "auth-service";

    // ── REGISTER ──────────────────────────────────────────────────────────────
    @Transactional
    @Override
    public MessageResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            auditService.registrar(
                    CategoriaAudit.ACTIVIDAD, "REGISTRO_FALLIDO",
                    request.getEmail(), "ANONIMO", "FALLIDO",
                    "Intento de registro con email ya existente",
                    "/api/v1/auth/register", null, SERVICIO);
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getPassword())) {
            throw new InvalidPasswordException(PasswordSeguroUtil.GetError());
        }

        ModelUsuario user = ModelUsuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .activo(true)
                .rol(ModelRoles.ROLE_USER)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ModelUsuario savedUser = userRepository.save(user);

        try {
            userClient.createUser(UserRequest.builder()
                    .email(request.getEmail())
                    .nombre(request.getNombre())
                    .dni(request.getDni())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .build());

            auditService.registrar(
                    CategoriaAudit.ACTIVIDAD, "REGISTRO_EXITOSO",
                    request.getEmail(), "ROLE_USER", "EXITOSO",
                    "Nuevo usuario registrado correctamente",
                    "/api/v1/auth/register", null, SERVICIO);

        } catch (Exception e) {
            log.error("Error creando perfil", e);
            userRepository.delete(savedUser);
            auditService.registrar(
                    CategoriaAudit.ALERTA, "REGISTRO_ROLLBACK",
                    request.getEmail(), "ROLE_USER", "FALLIDO",
                    "Rollback de registro: error al crear perfil en user-service",
                    "/api/v1/auth/register", null, SERVICIO);
            throw e;
        }

        return new MessageResponse("Usuario registrado correctamente");
    }

    // ── REGISTER INTERNO (staff/admin via user-service) ───────────────────────
    @Transactional
    @Override
    public void registrarConRol(InternalRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getPassword())) {
            throw new InvalidPasswordException(PasswordSeguroUtil.GetError());
        }

        ModelUsuario usuarioInterno = ModelUsuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(ModelRoles.valueOf(request.getRol()))
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        userRepository.save(usuarioInterno);

        auditService.registrarConMetadata(
                CategoriaAudit.CAMBIO_CRITICO, "CREAR_USUARIO_INTERNO",
                request.getEmail(), request.getRol(), "EXITOSO",
                "Credenciales internas creadas con rol " + request.getRol(),
                "/api/v1/auth/register-internal", null, SERVICIO,
                Map.of("rol", request.getRol(), "email", request.getEmail()));

        log.info("Credenciales internas creadas para: {}", request.getEmail());
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────
    @Override
    public MessageResponse logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización inválido");
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.getEmailFromToken(token);
            auditService.registrar(
                    CategoriaAudit.SESION, "LOGOUT",
                    email, null, "EXITOSO",
                    "Sesión cerrada correctamente",
                    "/api/v1/auth/logout", null, SERVICIO);
        } catch (Exception e) {
            log.warn("No se pudo extraer email del token en logout");
        }

        return new MessageResponse("Sesión cerrada correctamente");
    }

    // En AuthServiceImpl
    @Override
    public Map<String, Boolean> getSecurityStatus(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return Map.of(
                "otpActivado", user.isOtpActivado(),
                "twoFactorEnabled", user.isTwoFactorEnabled()
        );
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail();

        ModelUsuario user = userRepository.findByEmail(email).orElse(null);

        // Usuario no encontrado
        if (user == null) {
            auditService.registrar(
                    CategoriaAudit.SESION, "LOGIN_FALLIDO",
                    email, "ANONIMO", "FALLIDO",
                    "Intento de login con email no registrado",
                    "/api/v1/auth/login", null, SERVICIO);
            throw new RuntimeException("Usuario no encontrado");
        }

        // Contraseña incorrecta
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            // Verificar fuerza bruta: 5 intentos en 10 minutos
            boolean bruteForce = auditService.esAtaqueFuerzaBruta(email, "LOGIN_FALLIDO", 5, 10);

            auditService.registrar(
                    bruteForce ? CategoriaAudit.ALERTA : CategoriaAudit.SESION,
                    "LOGIN_FALLIDO",
                    email, user.getRol().name(), "FALLIDO",
                    bruteForce
                            ? " POSIBLE ATAQUE DE FUERZA BRUTA — múltiples intentos fallidos"
                            : "Contraseña incorrecta",
                    "/api/v1/auth/login", null, SERVICIO);

            throw new RuntimeException("Credenciales inválidas.");
        }

        // Cuenta desactivada
        if (!user.isActivo()) {
            auditService.registrar(
                    CategoriaAudit.SESION, "LOGIN_CUENTA_DESACTIVADA",
                    email, user.getRol().name(), "BLOQUEADO",
                    "Intento de login en cuenta desactivada",
                    "/api/v1/auth/login", null, SERVICIO);
            throw new RuntimeException("Cuenta desactivada");
        }

        // OTP requerido
        if (user.isOtpActivado()) {
            otpService.generateAndSendOtp(email);
            auditService.registrar(
                    CategoriaAudit.SESION, "LOGIN_OTP_REQUERIDO",
                    email, user.getRol().name(), "EXITOSO",
                    "OTP enviado para verificación de segundo factor",
                    "/api/v1/auth/login", null, SERVICIO);
            return AuthResponse.builder().otpRequired(true).email(email).build();
        }

        // 2FA requerido
        if (user.isTwoFactorEnabled()) {
            auditService.registrar(
                    CategoriaAudit.SESION, "LOGIN_2FA_REQUERIDO",
                    email, user.getRol().name(), "EXITOSO",
                    "2FA requerido para completar el login",
                    "/api/v1/auth/login", null, SERVICIO);
            return AuthResponse.builder().twoFactorRequired(true).email(email).build();
        }

        // Login exitoso
        String userServiceId = null;
        try {
            UserResponse userProfile = userClient.obtenerUsuarioPorEmail(email);
            if (userProfile != null && userProfile.getId() != null) {
                userServiceId = userProfile.getId().toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener userId del user-service: {}", e.getMessage());
        }

        String accessToken  = jwtUtil.generateToken(email, user.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(email);

        auditService.registrarConMetadata(
                CategoriaAudit.SESION, "LOGIN_EXITOSO",
                email, user.getRol().name(), "EXITOSO",
                "Login exitoso",
                "/api/v1/auth/login", null, SERVICIO,
                Map.of("rol", user.getRol().name(), "userId", userServiceId != null ? userServiceId : ""));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(email)
                .rol(user.getRol().name())
                .userId(userServiceId)
                .build();
    }
}