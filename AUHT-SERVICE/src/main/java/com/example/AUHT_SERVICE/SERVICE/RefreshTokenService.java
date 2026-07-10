package com.example.AUHT_SERVICE.SERVICE;

import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;

public interface RefreshTokenService {
    AuthResponse refreshToken(String refreshToken);
}
