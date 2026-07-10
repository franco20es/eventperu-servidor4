package com.example.AUHT_SERVICE.DTO.Response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginResponse {
    private String accessToken;
    private String refreshToken;
    private String email;
    private String rol;
    private String userId;
    private String proveedor;
    private boolean otpRequired;
    private boolean twoFactorRequired;
}