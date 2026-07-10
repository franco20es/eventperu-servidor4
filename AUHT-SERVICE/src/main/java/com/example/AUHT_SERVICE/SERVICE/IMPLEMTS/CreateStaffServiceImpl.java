package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;


import com.example.AUHT_SERVICE.DTO.Request.RegisterRequest;
import com.example.AUHT_SERVICE.DTO.Request.UserRequest;
import com.example.AUHT_SERVICE.DTO.Response.MessageResponse;
import com.example.AUHT_SERVICE.EXCEPTION.EmailAlreadyExistsException;
import com.example.AUHT_SERVICE.EXCEPTION.InvalidPasswordException;
import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import com.example.AUHT_SERVICE.MODEL.ModelRoles;
import com.example.AUHT_SERVICE.MODEL.ModelUsuario;
import com.example.AUHT_SERVICE.REPOSITORY.RepositoryUsuario;
import com.example.AUHT_SERVICE.SERVICE.AuditService;
import com.example.AUHT_SERVICE.SERVICE.CreateStaffService;
import com.example.AUHT_SERVICE.UTILS.PasswordSeguroUtil;
import com.example.AUHT_SERVICE.client.UserClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateStaffServiceImpl implements CreateStaffService {

    private final RepositoryUsuario userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final UserClient        userClient;
    private final AuditService auditService;

    private static final String SERVICIO = "auth-service";

    @Transactional
    @Override
    public MessageResponse createStaff(RegisterRequest request) {

        String adminEmail = getAdminEmail();

        if (userRepository.existsByEmail(request.getEmail())) {
            auditService.registrar(
                    CategoriaAudit.CAMBIO_CRITICO, "CREAR_STAFF_FALLIDO",
                    adminEmail, "ROLE_ADMIN", "FALLIDO",
                    "Intento de crear staff con email ya existente: " + request.getEmail(),
                    "/api/v1/auth/create/staff", null, SERVICIO);
            throw new EmailAlreadyExistsException("El email ya está registrado");
        }

        if (!PasswordSeguroUtil.validatePassword(request.getPassword())) {
            throw new InvalidPasswordException(PasswordSeguroUtil.GetError());
        }

        ModelUsuario staff = ModelUsuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(ModelRoles.ROLE_STAFF)
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        ModelUsuario savedStaff = userRepository.save(staff);

        try {
            userClient.createUser(UserRequest.builder()
                    .email(request.getEmail())
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .rol(ModelRoles.ROLE_STAFF.name())
                    .estado("ACTIVO")
                    .build());

            auditService.registrarConMetadata(
                    CategoriaAudit.CAMBIO_CRITICO, "CREAR_STAFF_EXITOSO",
                    adminEmail, "ROLE_ADMIN", "EXITOSO",
                    "Nuevo staff creado: " + request.getEmail(),
                    "/api/v1/auth/create/staff", null, SERVICIO,
                    Map.of("staffEmail", request.getEmail(),
                            "staffNombre", request.getNombre() + " " + request.getApellido(),
                            "creadoPor", adminEmail));

        } catch (Exception e) {
            log.error("Error creando staff en user-service", e);
            userRepository.delete(savedStaff);
            auditService.registrar(
                    CategoriaAudit.ALERTA, "CREAR_STAFF_ROLLBACK",
                    adminEmail, "ROLE_ADMIN", "FALLIDO",
                    "Rollback al crear staff " + request.getEmail() + ": " + e.getMessage(),
                    "/api/v1/auth/create/staff", null, SERVICIO);
            throw new RuntimeException("No se pudo crear el staff");
        }

        return new MessageResponse("Staff creado correctamente");
    }

    private String getAdminEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "admin-desconocido";
        }
    }
}