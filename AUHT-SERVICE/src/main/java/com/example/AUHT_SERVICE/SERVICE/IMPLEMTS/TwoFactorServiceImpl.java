package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Request.TwoFactorRequest;
import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.DTO.Response.QrResponse;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.TwoFactorService;
import com.example.AUHT_SERVICE.UTILS.JwtUtil;
import com.example.AUHT_SERVICE.UTILS.OtpUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFactorServiceImpl implements TwoFactorService {

    private final OtpUtil otpUtil;
    private final JwtUtil jwtUtil;
    private final UserClient userClient;
    private final RepositoryUsuario userRepository;

    @Override
    @Transactional
    public AuthResponse verify2FA(TwoFactorRequest request) {
        ModelUsuario user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        otpUtil.verifyCode(user.getSecret2FA(), request.getCodigo());

        String userServiceId = null;
        try {
            var userProfile = userClient.obtenerUsuarioPorEmail(user.getEmail());
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
                .twoFactorRequired(false)
                .build();
    }

    @Override
    @Transactional
    public QrResponse generarQrSinActivar(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo genera secret y QR, NO activa 2FA todavía
        String secret = otpUtil.generateSecret();
        user.setSecret2FA(secret);
        user.setTwoFactorEnabled(false); // ← no activar aún
        userRepository.save(user);

        log.info("QR generado sin activar para: {}", email);

        String qrImage = otpUtil.generateQrImage(email, secret);

        return QrResponse.builder()
                .qrImage(qrImage)
                .secret(secret)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse activar2FA(String email, String codigo) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.getSecret2FA() == null) {
            throw new RuntimeException("Primero genera el QR");
        }

        // Verificar código antes de activar
        otpUtil.verifyCode(user.getSecret2FA(), codigo);

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        log.info("2FA activado para: {}", email);
        return new MessageResponse("2FA activado correctamente");
    }

    @Override
    @Transactional
    public MessageResponse desactivar2FA(String email) {
        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setTwoFactorEnabled(false);
        user.setSecret2FA(null);
        userRepository.save(user);
        log.info("2FA desactivado para: {}", email);
        return new MessageResponse("2FA desactivado correctamente");
    }
}