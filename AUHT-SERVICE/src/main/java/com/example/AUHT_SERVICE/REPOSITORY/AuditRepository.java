package com.example.AUHT_SERVICE.REPOSITORY;


import com.example.AUHT_SERVICE.MODEL.AuditLog;
import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByCategoria(CategoriaAudit categoria, Pageable pageable);

    Page<AuditLog> findByEmail(String email, Pageable pageable);

    @Query("""
    SELECT a FROM AuditLog a
    WHERE (:email     IS NULL OR a.email     LIKE %:email%)
      AND (:categoria IS NULL OR a.categoria  = :categoria)
      AND (:resultado IS NULL OR a.resultado  = :resultado)
      AND (:desde     IS NULL OR a.createdAt >= :desde)
      AND (:hasta     IS NULL OR a.createdAt <= :hasta)
    """)
    Page<AuditLog> buscar(
            @Param("email")     String email,
            @Param("categoria") CategoriaAudit categoria,
            @Param("resultado") String resultado,
            @Param("desde")     LocalDateTime desde,
            @Param("hasta")     LocalDateTime hasta,
            Pageable pageable);

    // Alertas: más de N intentos fallidos en los últimos X minutos
    @Query("""
        SELECT COUNT(a) FROM AuditLog a
        WHERE a.email    = :email
          AND a.accion   = :accion
          AND a.resultado = 'FALLIDO'
          AND a.createdAt >= :desde
        """)
    long contarIntentosFallidos(
            @Param("email")  String email,
            @Param("accion") String accion,
            @Param("desde")  LocalDateTime desde);

    List<AuditLog> findTop20ByCategoriaOrderByCreatedAtDesc(CategoriaAudit categoria);
}