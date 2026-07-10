package com.example.AUHT_SERVICE.MODEL;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_sistema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionModel {

    @Id
    @Column(length = 100)
    private String clave;           // ej: SISTEMA_NOMBRE, PAGOS_COMISION

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;           // valor como String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaConfig categoria;

    @Column(length = 200)
    private String descripcion;     // descripción legible

    @Column(length = 30)
    private String tipo;            // TEXT | NUMBER | BOOLEAN | URL | EMAIL

    @Column(nullable = false)
    private Boolean editable = true; // algunas claves son solo lectura

    @Column(nullable = false)
    private Boolean sensible = false; // si true, ocultar valor en el frontend

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String updatedBy;       // email del admin que lo modificó
}