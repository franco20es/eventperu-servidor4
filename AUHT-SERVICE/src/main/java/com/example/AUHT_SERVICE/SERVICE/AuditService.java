package com.example.AUHT_SERVICE.SERVICE;


import com.example.AUHT_SERVICE.MODEL.AuditLog;
import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {

    /** Registra cualquier evento de auditoría. */
    void registrar(CategoriaAudit categoria, String accion, String email,
                   String rol, String resultado, String detalle,
                   String recurso, String ip, String servicio);

    /** Registra con metadata adicional en JSON. */
    void registrarConMetadata(CategoriaAudit categoria, String accion, String email,
                              String rol, String resultado, String detalle,
                              String recurso, String ip, String servicio,
                              Map<String, Object> metadata);

    /** Lista paginada con filtros. */
    Page<AuditLog> listar(String email, String categoria, String resultado,
                          LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    /** KPIs de auditoría para el dashboard. */
    Map<String, Object> getKpis();

    /** Últimas 20 alertas críticas. */
    List<AuditLog> getUltimasAlertas();

    /** Verifica si un email tiene demasiados intentos fallidos (brute force). */
    boolean esAtaqueFuerzaBruta(String email, String accion, int maxIntentos, int minutos);


}