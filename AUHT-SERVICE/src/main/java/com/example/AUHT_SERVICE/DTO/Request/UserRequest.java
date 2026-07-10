package com.example.AUHT_SERVICE.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String rol;
    private String estado;
    private String dni;
    private Set<String> preferenciasNotificacion;
}
