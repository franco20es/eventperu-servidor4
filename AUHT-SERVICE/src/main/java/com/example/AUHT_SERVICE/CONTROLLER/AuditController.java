package com.example.AUHT_SERVICE.CONTROLLER;


import com.example.AUHT_SERVICE.MODEL.AuditLog;
import com.example.AUHT_SERVICE.SERVICE.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    // ── KPIs ──────────────────────────────────────────────────────────────────
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        log.info("GET /audit/kpis");
        return ResponseEntity.ok(auditService.getKpis());
    }

    // ── Listar paginado ───────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Page<AuditLog>> listar(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "20")         int size) {

        log.info("GET /audit page={} categoria={}", page, categoria);

        return ResponseEntity.ok(auditService.listar(
                email, categoria, resultado, desde, hasta,
                PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    // ── Últimas alertas ───────────────────────────────────────────────────────
    @GetMapping("/alertas")
    public ResponseEntity<List<AuditLog>> getAlertas() {
        log.info("GET /audit/alertas");
        return ResponseEntity.ok(auditService.getUltimasAlertas());
    }
}