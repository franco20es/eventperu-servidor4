package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Request.OtpRequest;
import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.EmailService;
import com.example.AUHT_SERVICE.SERVICE.OtpService;
import com.example.AUHT_SERVICE.UTILS.EmailTemplateBuilder;
import com.example.AUHT_SERVICE.UTILS.JwtUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final RepositoryUsuario userRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final UserClient userClient;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void generateAndSendOtp(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar OTP de 6 dígitos
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        // ← Guardar en BD en vez de Redis
        user.setCodigoOtp(code);
        user.setExpiracionOtp(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String htmlContent = EmailTemplateBuilder.buildOtpTemplate(
                "SEGURIDAD",
                "Usa el siguiente código para validar tu acceso a EventPeru.",
                code
        );

        emailService.sendEmail(email, "Tu código de seguridad — EventPeru", htmlContent);
        log.info("OTP enviado a: {}", email);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(OtpRequest request) {
        String email = request.getEmail();

        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getCodigoOtp() == null || !user.getCodigoOtp().equals(request.getCodigo().trim())) {
            throw new RuntimeException("Código OTP inválido");
        }

        if (user.getExpiracionOtp() == null || user.getExpiracionOtp().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código OTP expirado");
        }

        user.setCodigoOtp(null);
        user.setExpiracionOtp(null);
        user.setUltimoLogin(LocalDateTime.now());
        userRepository.save(user);

        if (user.isTwoFactorEnabled()) {
            return AuthResponse.builder()
                    .twoFactorRequired(true)
                    .email(email)
                    .build();
        }

        String userServiceId = null;
        try {
            var userProfile = userClient.obtenerUsuarioPorEmail(email);
            if (userProfile != null && userProfile.getId() != null) {
                userServiceId = userProfile.getId().toString();
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener userId del user-service: {}", e.getMessage());
        }

        String accessToken  = jwtUtil.generateToken(user.getEmail(), user.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .rol(user.getRol().name())
                .userId(userServiceId)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse activarOtp(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setOtpActivado(true);
        userRepository.save(user);
        log.info("OTP activado para: {}", email);
        return new MessageResponse("OTP activado correctamente");
    }

    @Override
    @Transactional
    public MessageResponse desactivarOtp(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setOtpActivado(false);
        userRepository.save(user);
        log.info("OTP desactivado para: {}", email);
        return new MessageResponse("OTP desactivado correctamente");
    }
}