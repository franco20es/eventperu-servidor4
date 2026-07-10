package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.RefreshTokenService;
import com.example.AUHT_SERVICE.UTILS.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final RepositoryUsuario userRepository;

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        String email = jwtUtil.getEmailFromToken(refreshToken);

        ModelUsuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.isActivo()) {
            throw new RuntimeException("Cuenta desactivada");
        }

        String newAccessToken  = jwtUtil.generateToken(email, user.getRol().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        log.info("Tokens renovados para: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(email)
                .rol(user.getRol().name())
                .build();
    }
}