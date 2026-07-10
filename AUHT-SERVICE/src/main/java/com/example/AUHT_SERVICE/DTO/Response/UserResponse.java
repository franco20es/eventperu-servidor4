package com.example.AUHT_SERVICE.DTO.Response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String dni;
}