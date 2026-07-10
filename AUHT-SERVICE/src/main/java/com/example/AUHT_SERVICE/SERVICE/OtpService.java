package com.example.AUHT_SERVICE.SERVICE;

import com.example.AUHT_SERVICE.DTO.Request.OtpRequest;
import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;

public interface OtpService {

    void generateAndSendOtp(String email);

    AuthResponse verifyOtp(OtpRequest request);

    MessageResponse activarOtp(String email);

    MessageResponse desactivarOtp(String email);
}