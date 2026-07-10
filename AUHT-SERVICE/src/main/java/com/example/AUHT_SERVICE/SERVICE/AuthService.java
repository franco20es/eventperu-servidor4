package com.example.AUHT_SERVICE.SERVICE;

import com.example.AUHT_SERVICE.DTO.Request.*;
import com.example.AUHT_SERVICE.DTO.Response.AuthResponse;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;

import java.util.Map;

public interface AuthService {

    // Metodo para validar el login
    AuthResponse login(LoginRequest request);

    Map<String, Boolean> getSecurityStatus(String email);
    // Metodo para registrar un nuevo usuario
    MessageResponse register(RegisterRequest request);

    // Metodo para validar el token y obtener la informacion del usuario
    MessageResponse logout(String authHeader);

    // NUEVO: Método para el registro interno (Staff/Admin)
    void registrarConRol(InternalRegisterRequest request);
}


