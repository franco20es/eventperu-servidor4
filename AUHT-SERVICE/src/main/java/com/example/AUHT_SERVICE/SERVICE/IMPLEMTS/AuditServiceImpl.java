package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;


import com.example.AUHT_SERVICE.MODEL.AuditLog;
import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import com.example.AUHT_SERVICE.REPOSITORY.AuditRepository;
import com.example.AUHT_SERVICE.SERVICE.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepo;
    private final ObjectMapper    objectMapper;

    // ── Registrar ─────────────────────────────────────────────────────────────
    @Override
    @Async
    @Transactional
    public void registrar(CategoriaAudit categoria, String accion, String email,
                          String rol, String resultado, String detalle,
                          String recurso, String ip, String servicio) {

        try {
            AuditLog auditLog = AuditLog.builder()  // ← cambia "log" por "auditLog"
                    .categoria(categoria)
                    .accion(accion)
                    .email(email)
                    .rol(rol)
                    .resultado(resultado)
                    .detalle(detalle)
                    .recurso(recurso)
                    .ip(ip)
                    .servicio(servicio)
                    .build();

            auditRepo.save(auditLog);  // ← aquí también
        } catch (Exception e) {
            log.error("Error guardando audit log: {}", e.getMessage()); // ← este "log" es el logger
        }
    }

    // ── Registrar con metadata ────────────────────────────────────────────────
    @Override
    @Async
    @Transactional
    public void registrarConMetadata(CategoriaAudit categoria, String accion, String email,
                                     String rol, String resultado, String detalle,
                                     String recurso, String ip, String servicio,
                                     Map<String, Object> metadata) {
        try {
            String metaJson = objectMapper.writeValueAsString(metadata);
            AuditLog auditLog = AuditLog.builder()
                    .categoria(categoria)
                    .accion(accion)
                    .email(email)
                    .rol(rol)
                    .resultado(resultado)
                    .detalle(detalle)
                    .recurso(recurso)
                    .ip(ip)
                    .servicio(servicio)
                    .metadata(metaJson)
                    .build();
            auditRepo.save(auditLog);
        } catch (Exception e) {
            log.error("Error guardando audit log con metadata: {}", e.getMessage());
        }
    }

    // ── Listar ────────────────────────────────────────────────────────────────
    @Override
    public Page<AuditLog> listar(String email, String categoria, String resultado,
                                 LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {

        List<AuditLog> all = auditRepo.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt"));

        CategoriaAudit cat = null;
        if (categoria != null && !categoria.isBlank()) {
            try { cat = CategoriaAudit.valueOf(categoria); }
            catch (Exception ignored) {}
        }

        final CategoriaAudit catFinal = cat;

        List<AuditLog> filtrado = all.stream()
                .filter(a -> email == null || email.isBlank() ||
                        (a.getEmail() != null && a.getEmail().toLowerCase().contains(email.toLowerCase())))
                .filter(a -> catFinal == null || a.getCategoria() == catFinal)
                .filter(a -> resultado == null || resultado.isBlank() ||
                        resultado.equals(a.getResultado()))
                .filter(a -> desde == null || (a.getCreatedAt() != null && !a.getCreatedAt().isBefore(desde)))
                .filter(a -> hasta == null || (a.getCreatedAt() != null && !a.getCreatedAt().isAfter(hasta)))
                .collect(Collectors.toList());

        int total = filtrado.size();
        int from  = (int) pageable.getOffset();
        int to    = Math.min(from + pageable.getPageSize(), total);

        List<AuditLog> pagina = from > total
                ? Collections.emptyList()
                : filtrado.subList(from, to);

        return new PageImpl<>(pagina, pageable, total);
    }



    // ── KPIs ──────────────────────────────────────────────────────────────────
    @Override
    public Map<String, Object> getKpis() {
        List<AuditLog> all = auditRepo.findAll();

        long total        = all.size();
        long exitosos     = all.stream().filter(a -> "EXITOSO".equals(a.getResultado())).count();
        long fallidos     = all.stream().filter(a -> "FALLIDO".equals(a.getResultado())).count();
        long alertas      = all.stream().filter(a -> a.getCategoria() == CategoriaAudit.ALERTA).count();
        long noAutorizados = all.stream().filter(a -> a.getCategoria() == CategoriaAudit.NO_AUTORIZADO).count();
        long cambiosCriticos = all.stream().filter(a -> a.getCategoria() == CategoriaAudit.CAMBIO_CRITICO).count();

        // Hoy
        LocalDateTime hoy = LocalDateTime.now().toLocalDate().atStartOfDay();
        long eventosHoy = all.stream().filter(a -> a.getCreatedAt() != null &&
                a.getCreatedAt().isAfter(hoy)).count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("total",           total);
        kpis.put("exitosos",        exitosos);
        kpis.put("fallidos",        fallidos);
        kpis.put("alertas",         alertas);
        kpis.put("noAutorizados",   noAutorizados);
        kpis.put("cambiosCriticos", cambiosCriticos);
        kpis.put("eventosHoy",      eventosHoy);
        return kpis;
    }

    // ── Últimas alertas ───────────────────────────────────────────────────────
    @Override
    public List<AuditLog> getUltimasAlertas() {
        return auditRepo.findTop20ByCategoriaOrderByCreatedAtDesc(CategoriaAudit.ALERTA);
    }

    // ── Brute force detector ──────────────────────────────────────────────────
    @Override
    public boolean esAtaqueFuerzaBruta(String email, String accion, int maxIntentos, int minutos) {
        LocalDateTime desde = LocalDateTime.now().minusMinutes(minutos);
        long intentos = auditRepo.contarIntentosFallidos(email, accion, desde);
        return intentos >= maxIntentos;
    }

    private String nvl(String s) { return (s == null || s.isBlank()) ? null : s; }
}