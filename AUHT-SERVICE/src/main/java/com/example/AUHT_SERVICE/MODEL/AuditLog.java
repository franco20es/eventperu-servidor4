package com.example.AUHT_SERVICE.MODEL;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_email",     columnList = "email"),
        @Index(name = "idx_audit_categoria", columnList = "categoria"),
        @Index(name = "idx_audit_created",   columnList = "createdAt"),
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // ── Quién ─────────────────────────────────────────────────────
    @Column(length = 150)
    private String email;           // usuario que realizó la acción

    @Column(length = 50)
    private String rol;             // ROLE_USER | ROLE_ADMIN | ROLE_STAFF | ANONIMO

    @Column(length = 50)
    private String ip;              // IP del cliente

    @Column(length = 200)
    private String userAgent;       // navegador / dispositivo

    // ── Qué ───────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaAudit categoria;  // ACTIVIDAD | SESION | NO_AUTORIZADO | CAMBIO_CRITICO | ALERTA

    @Column(nullable = false, length = 100)
    private String accion;          // LOGIN_EXITOSO | CREAR_STAFF | TICKET_DOBLE_USO, etc.

    @Column(length = 500)
    private String detalle;         // descripción legible

    @Column(length = 100)
    private String recurso;         // endpoint o entidad afectada

    @Column(length = 50)
    private String resultado;       // EXITOSO | FALLIDO | BLOQUEADO

    // ── Contexto extra ────────────────────────────────────────────
    @Column(length = 50)
    private String servicio;        // auth-service | payment-service | etc.

    @Column(columnDefinition = "TEXT")
    private String metadata;        // JSON con datos adicionales (opcional)

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}