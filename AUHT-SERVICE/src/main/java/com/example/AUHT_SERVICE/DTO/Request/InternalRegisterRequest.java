package com.example.AUHT_SERVICE.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalRegisterRequest {
    private String email;
    private String password;
    private String rol;
}