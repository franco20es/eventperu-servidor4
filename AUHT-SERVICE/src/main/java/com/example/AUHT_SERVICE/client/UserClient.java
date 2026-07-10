package com.example.AUHT_SERVICE.client;

import com.example.AUHT_SERVICE.DTO.Request.UserRequest;
import com.example.AUHT_SERVICE.DTO.Response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Eliminamos la URL hardcoded para usar el Discovery Service (Eureka)
@FeignClient(name = "user-service")
public interface UserClient {

//    @PostMapping("/api/v1/users") // Alineado con la versión v1 que definimos
//    void createUser(@RequestBody UserRequest request);

    @PostMapping("/api/v1/users/internal")
    void createUser(@RequestBody UserRequest request);

    @GetMapping("/api/v1/users/internal/email/{email}")
    UserResponse obtenerUsuarioPorEmail(@PathVariable("email") String email);
}