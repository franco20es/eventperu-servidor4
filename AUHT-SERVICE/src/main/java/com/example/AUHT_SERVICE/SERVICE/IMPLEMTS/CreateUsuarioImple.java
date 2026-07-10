package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;

import com.example.AUHT_SERVICE.DTO.Request.RegisterRequest;
import com.example.AUHT_SERVICE.DTO.Request.UserRequest;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.EXCEPTION.EmailAlreadyExistsException;
import com.example.AUHT_SERVICE.EXCEPTION.InvalidPasswordException;
import com.example.AUHT_SERVICE.MODEL.ModelRoles;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.CreateAdminService;
import com.example.AUHT_SERVICE.SERVICE.CreateUsuarioService;
import com.example.AUHT_SERVICE.UTILS.PasswordSeguroUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateUsuarioImple implements CreateUsuarioService {

    private final RepositoryUsuario userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserClient userClient;

    @Transactional
    @Override
    public MessageResponse CreateUsuario(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getPassword())) {
            throw new InvalidPasswordException(PasswordSeguroUtil.GetError());
        }

        ModelUsuario admin = ModelUsuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(ModelRoles.ROLE_USER)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ModelUsuario savedAdmin = userRepository.save(admin);

        try {
            UserRequest userRequest = UserRequest.builder()
                    .email(request.getEmail())
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .rol(ModelRoles.ROLE_USER.name())
                    .estado("ACTIVO")
                    .build();

            userClient.createUser(userRequest);

        } catch (Exception e) {
            log.error("Error creando Usuario en user-service", e);
            userRepository.delete(savedAdmin);
            throw new RuntimeException("No se pudo crear el usuario ");
        }

        return new MessageResponse("Usuario creado correctamente");
    }
}