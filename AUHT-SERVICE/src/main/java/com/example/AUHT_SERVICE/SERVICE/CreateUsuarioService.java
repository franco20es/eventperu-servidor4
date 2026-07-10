package com.example.AUHT_SERVICE.SERVICE;

import com.example.AUHT_SERVICE.DTO.Request.RegisterRequest;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;

public interface CreateUsuarioService {

    MessageResponse CreateUsuario(RegisterRequest request);
}
