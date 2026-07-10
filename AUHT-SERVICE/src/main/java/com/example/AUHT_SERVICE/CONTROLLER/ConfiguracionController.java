package com.example.AUHT_SERVICE.CONTROLLER;


import com.example.AUHT_SERVICE.MODEL.CategoriaConfig;
import com.example.AUHT_SERVICE.MODEL.ConfiguracionModel;
import com.example.AUHT_SERVICE.SERVICE.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionController {

    private final ConfiguracionService configService;

    // ── Todas agrupadas por categoría ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<Map<String, List<ConfiguracionModel>>> getTodas() {
        log.info("GET /config");
        return ResponseEntity.ok(configService.getTodasAgrupadas());
    }

    // ── Por categoría ─────────────────────────────────────────────────────────
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ConfiguracionModel>> getPorCategoria(
            @PathVariable String categoria) {
        log.info("GET /config/categoria/{}", categoria);
        try {
            CategoriaConfig cat = CategoriaConfig.valueOf(categoria.toUpperCase());
            return ResponseEntity.ok(configService.getPorCategoria(cat));
        } catch (Exception e) {
            throw new RuntimeException("Categoría inválida: " + categoria);
        }
    }

    // ── Actualizar una clave ──────────────────────────────────────────────────
    @PutMapping("/{clave}")
    public ResponseEntity<ConfiguracionModel> actualizar(
            @PathVariable String clave,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String emailAdmin = auth != null ? auth.getName() : "admin";
        String valor = body.get("valor");
        log.info("PUT /config/{} por {}", clave, emailAdmin);
        return ResponseEntity.ok(configService.actualizar(clave, valor, emailAdmin));
    }

    // ── Actualizar bloque (toda una categoría) ────────────────────────────────
    @PutMapping("/bloque")
    public ResponseEntity<List<ConfiguracionModel>> actualizarBloque(
            @RequestBody Map<String, String> valores,
            Authentication auth) {
        String emailAdmin = auth != null ? auth.getName() : "admin";
        log.info("PUT /config/bloque - {} claves por {}", valores.size(), emailAdmin);
        return ResponseEntity.ok(configService.actualizarBloque(valores, emailAdmin));
    }
}