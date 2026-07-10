package com.example.AUHT_SERVICE.SERVICE.IMPLEMTS;


import com.example.AUHT_SERVICE.MODEL.CategoriaAudit;
import com.example.AUHT_SERVICE.MODEL.CategoriaConfig;
import com.example.AUHT_SERVICE.MODEL.ConfiguracionModel;
import com.example.AUHT_SERVICE.REPOSITORY.ConfiguracionRepository;
import com.example.AUHT_SERVICE.SERVICE.AuditService;
import com.example.AUHT_SERVICE.SERVICE.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository repo;
    private final AuditService auditService;

    private static final String SERVICIO = "auth-service";

    // ── Obtener todas agrupadas ───────────────────────────────────────────────
    @Override
    public Map<String, List<ConfiguracionModel>> getTodasAgrupadas() {
        List<ConfiguracionModel> todas = repo.findAll();

        // Ocultar valores sensibles
        todas.forEach(c -> {
            if (Boolean.TRUE.equals(c.getSensible()) && !c.getValor().isBlank()) {
                c.setValor("••••••••");
            }
        });

        return todas.stream().collect(
                Collectors.groupingBy(c -> c.getCategoria().name()));
    }

    // ── Por categoría ─────────────────────────────────────────────────────────
    @Override
    public List<ConfiguracionModel> getPorCategoria(CategoriaConfig categoria) {
        return repo.findByCategoriaOrderByClave(categoria).stream()
                .peek(c -> {
                    if (Boolean.TRUE.equals(c.getSensible()) && !c.getValor().isBlank()) {
                        c.setValor("••••••••");
                    }
                })
                .collect(Collectors.toList());
    }

    // ── Getters de valor ──────────────────────────────────────────────────────
    @Override
    public String getValor(String clave) {
        return repo.findByClave(clave)
                .map(ConfiguracionModel::getValor)
                .orElse("");
    }

    @Override
    public double getValorNumerico(String clave, double defaultVal) {
        try { return Double.parseDouble(getValor(clave)); }
        catch (Exception e) { return defaultVal; }
    }

    @Override
    public boolean getValorBooleano(String clave, boolean defaultVal) {
        String val = getValor(clave);
        if (val.isBlank()) return defaultVal;
        return "true".equalsIgnoreCase(val);
    }

    // ── Actualizar ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ConfiguracionModel actualizar(String clave, String valor, String emailAdmin) {
        ConfiguracionModel config = repo.findByClave(clave)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada: " + clave));

        if (!Boolean.TRUE.equals(config.getEditable())) {
            throw new RuntimeException("La configuración '" + clave + "' no es editable");
        }

        String valorAnterior = config.getValor();
        config.setValor(valor);
        config.setUpdatedBy(emailAdmin);

        ConfiguracionModel saved = repo.save(config);

        auditService.registrarConMetadata(
                CategoriaAudit.CAMBIO_CRITICO, "CONFIG_ACTUALIZADA",
                emailAdmin, "ROLE_ADMIN", "EXITOSO",
                "Configuración actualizada: " + clave,
                "/api/v1/config", null, SERVICIO,
                Map.of("clave", clave,
                        "anterior", Boolean.TRUE.equals(config.getSensible()) ? "••••••••" : valorAnterior,
                        "nuevo",    Boolean.TRUE.equals(config.getSensible()) ? "••••••••" : valor));

        log.info("Config actualizada: {} por {}", clave, emailAdmin);
        return saved;
    }

    // ── Actualizar bloque ─────────────────────────────────────────────────────
    @Override
    @Transactional
    public List<ConfiguracionModel> actualizarBloque(Map<String, String> valores, String emailAdmin) {
        List<ConfiguracionModel> actualizados = new ArrayList<>();

        valores.forEach((clave, valor) -> {
            try {
                actualizados.add(actualizar(clave, valor, emailAdmin));
            } catch (Exception e) {
                log.warn("No se pudo actualizar {}: {}", clave, e.getMessage());
            }
        });

        return actualizados;
    }
}