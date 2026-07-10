package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Request.CambiarPasswordPerfilRequest;
import com.example.AUHT_SERVICE.DTO.Request.CambiarPasswordRequest;
import com.example.AUHT_SERVICE.DTO.Request.RecuperarPasswordRequest;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.EmailService;
import com.example.AUHT_SERVICE.SERVICE.PasswordService;
import com.example.AUHT_SERVICE.UTILS.EmailTemplateBuilder;
import com.example.AUHT_SERVICE.UTILS.PasswordSeguroUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final RepositoryUsuario userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional
    public MessageResponse recuperarPassword(RecuperarPasswordRequest request) {
        ModelUsuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // ← Enlace con botón clickeable
        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;
        String htmlContent = EmailTemplateBuilder.buildResetPasswordTemplate(resetLink);

        emailService.sendEmail(
                request.getEmail(),
                "Recupera tu contraseña — EventPeru",
                htmlContent
        );

        log.info("Enlace de recuperación enviado a: {}", request.getEmail());
        return new MessageResponse("Se ha enviado un enlace de recuperación a tu email");
    }
    @Override
    @Transactional
    public MessageResponse cambiarPasswordPerfil(CambiarPasswordPerfilRequest request) {
        ModelUsuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getPasswordNuevo())) {
            throw new RuntimeException(PasswordSeguroUtil.GetError());
        }

        user.setPassword(passwordEncoder.encode(request.getPasswordNuevo()));
        user.setFechaActualizacion(LocalDateTime.now());
        userRepository.save(user);

        log.info("Contraseña de perfil actualizada para: {}", user.getEmail());
        return new MessageResponse("Contraseña actualizada correctamente");
    }

    @Override
    @Transactional
    public MessageResponse changePassword(CambiarPasswordRequest request) {
        ModelUsuario user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token de recuperación inválido o inexistente"));

        if (user.getResetPasswordTokenExpiry() == null ||
                user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token de recuperación ha expirado");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getNewPassword())) {
            throw new RuntimeException(PasswordSeguroUtil.GetError());
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        user.setFechaActualizacion(LocalDateTime.now());
        userRepository.save(user);

        log.info("Contraseña actualizada para: {}", user.getEmail());
        return new MessageResponse("Contraseña actualizada correctamente");
    }
}