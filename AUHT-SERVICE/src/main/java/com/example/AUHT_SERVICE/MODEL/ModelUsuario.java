package com.example.AUHT_SERVICE.MODEL;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ModelUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  DATOS BÁSICOS
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModelRoles rol;

    //  ESTADO DE LA CUENTA
    @Builder.Default
    private boolean activo = true;
    @Builder.Default
    private boolean bloqueado = false;

    //  CONTROL DE INTENTOS
    @Builder.Default
    private Integer intentosFallidos = 0;
    private LocalDateTime bloqueadoHasta;

    //  VERIFICACIÓN EMAIL
    @Builder.Default
    private boolean emailVerificado = false;
    private String tokenVerificacion;

    //  OTP POR EMAIL
    private String codigoOtp;
    private LocalDateTime expiracionOtp;

    @Builder.Default
    private boolean otpActivado = false;  // ← AGREGAR

    //  GOOGLE AUTHENTICATOR (TOTP)
    @Builder.Default
    private boolean twoFactorEnabled = false;
    private String secret2FA;

    //  RECUPERACIÓN DE CONTRASEÑA
    private String resetPasswordToken;
    private LocalDateTime resetPasswordTokenExpiry;


    //  AUDITORÍA (MUY IMPORTANTE)
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;
    private LocalDateTime ultimoLogin;
}