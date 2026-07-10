package com.example.AUHT_SERVICE.CONTROLLER;

import com.example.AUHT_SERVICE.DTO.Request.*;
import com.example.AUHT_SERVICE.DTO.Response.*;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.SERVICE.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final TwoFactorService twoFactorService;
    private final GoogleAuthService googleAuthService;
    private final PasswordService passwordService;
    private final RefreshTokenService refreshTokenService;
    private final CreateAdminService createAdminService;
    private final CreateUsuarioService createUsuarioService;
    private final AccionesService accionesService;
    private final CreateStaffService createStaffService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.verifyOtp(request));
    }

    @PostMapping("/2fa")
    public ResponseEntity<AuthResponse> verify2FA(@Valid @RequestBody TwoFactorRequest request) {
        return ResponseEntity.ok(twoFactorService.verify2FA(request));
    }

    // ← QR sin activar 2FA
    @GetMapping("/qr")
    public ResponseEntity<QrResponse> generateQr(@RequestParam String email) {
        return ResponseEntity.ok(twoFactorService.generarQrSinActivar(email));
    }

    // ← Activar 2FA (confirmar con código)
    @PostMapping("/2fa/activar")
    public ResponseEntity<MessageResponse> activar2FA(
            @RequestParam String email,
            @RequestParam String codigo) {
        return ResponseEntity.ok(twoFactorService.activar2FA(email, codigo));
    }

    // ← Desactivar 2FA
    @PostMapping("/2fa/desactivar")
    public ResponseEntity<MessageResponse> desactivar2FA(@RequestParam String email) {
        return ResponseEntity.ok(twoFactorService.desactivar2FA(email));
    }

    // ← Activar OTP por email
    @PostMapping("/otp/activar")
    public ResponseEntity<MessageResponse> activarOtp(@RequestParam String email) {
        return ResponseEntity.ok(otpService.activarOtp(email));
    }

    // ← Desactivar OTP por email
    @PostMapping("/otp/desactivar")
    public ResponseEntity<MessageResponse> desactivarOtp(@RequestParam String email) {
        return ResponseEntity.ok(otpService.desactivarOtp(email));
    }

    @PostMapping("/google/login")
    public ResponseEntity<GoogleLoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(request));
    }

    @PostMapping("/google/register")
    public ResponseEntity<GoogleLoginResponse> registerWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(googleAuthService.registerWithGoogle(request));
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<MessageResponse> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        return ResponseEntity.ok(passwordService.recuperarPassword(request));
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody CambiarPasswordRequest request) {
        return ResponseEntity.ok(passwordService.changePassword(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authService.logout(authHeader));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/create/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(createAdminService.createAdmin(request));
    }

    @PostMapping("/create/user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(createUsuarioService.CreateUsuario(request));
    }

    @PutMapping("/{email}/desactivar")
    public ResponseEntity<Void> desactivarUsuario(@PathVariable String email) {
        accionesService.desactivarCuenta(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{email}/reactivar")
    public ResponseEntity<Void> reactivarUsuario(@PathVariable String email) {
        accionesService.reactivarCuenta(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cambiar-password-perfil")
    public ResponseEntity<MessageResponse> cambiarPasswordPerfil(
            @Valid @RequestBody CambiarPasswordPerfilRequest request) {
        return ResponseEntity.ok(passwordService.cambiarPasswordPerfil(request));
    }


    @PostMapping("/register-internal")
    public ResponseEntity<Void> registrarInterno(@RequestBody InternalRegisterRequest request) {
        authService.registrarConRol(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/create/staff")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> createStaff(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(createStaffService.createStaff(request));
    }

    @GetMapping("/security-status")
    public ResponseEntity<Map<String, Boolean>> getSecurityStatus(@RequestParam String email) {
        return ResponseEntity.ok(authService.getSecurityStatus(email));
    }
}