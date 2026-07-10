package com.example.AUHT_SERVICE.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CambiarPasswordPerfilRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String passwordActual;
    @NotBlank
    private String passwordNuevo;
}