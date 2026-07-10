package com.example.AUHT_SERVICE.CONFIG;


import com.example.AUHT_SERVICE.MODEL.CategoriaConfig;
import com.example.AUHT_SERVICE.MODEL.ConfiguracionModel;
import com.example.AUHT_SERVICE.REPOSITORY.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionDataLoader implements ApplicationRunner {

    private final ConfiguracionRepository repo;

    @Override
    public void run(ApplicationArguments args) {
        long count = repo.count();
        if (count > 0) {
            log.info("Configuración del sistema ya inicializada ({} registros)", count);
            return;
        }

        log.info("Inicializando configuración del sistema...");

        List<ConfiguracionModel> defaults = List.of(

                // ── SISTEMA ──────────────────────────────────────────────────────
                cfg("SISTEMA_NOMBRE",          "EventPeru",           CategoriaConfig.SISTEMA,  "Nombre de la plataforma",                  "TEXT",    true,  false),
                cfg("SISTEMA_LOGO_URL",        "",                    CategoriaConfig.SISTEMA,  "URL del logo principal",                   "URL",     true,  false),
                cfg("SISTEMA_MONEDA",          "PEN",                 CategoriaConfig.SISTEMA,  "Moneda del sistema (ISO 4217)",             "TEXT",    true,  false),
                cfg("SISTEMA_ZONA_HORARIA",    "America/Lima",        CategoriaConfig.SISTEMA,  "Zona horaria del sistema",                 "TEXT",    true,  false),
                cfg("SISTEMA_EMAIL_SOPORTE",   "soporte@eventperu.com", CategoriaConfig.SISTEMA, "Email de soporte al cliente",             "EMAIL",   true,  false),
                cfg("SISTEMA_MANTENIMIENTO",   "false",               CategoriaConfig.SISTEMA,  "Activar modo mantenimiento",               "BOOLEAN", true,  false),
                cfg("SISTEMA_MSG_MANTENIMIENTO","Plataforma en mantenimiento. Vuelve pronto.", CategoriaConfig.SISTEMA, "Mensaje modo mantenimiento", "TEXT", true, false),

                // ── PAGOS ─────────────────────────────────────────────────────────
                cfg("PAGOS_COMISION_PCT",      "10",                  CategoriaConfig.PAGOS,    "Comisión de la plataforma (%)",            "NUMBER",  true,  false),
                cfg("PAGOS_MP_PUBLIC_KEY",     "",                    CategoriaConfig.PAGOS,    "Mercado Pago Public Key",                  "TEXT",    true,  true),
                cfg("PAGOS_MP_ACCESS_TOKEN",   "",                    CategoriaConfig.PAGOS,    "Mercado Pago Access Token",                "TEXT",    true,  true),
                cfg("PAGOS_WEBHOOK_URL",       "",                    CategoriaConfig.PAGOS,    "URL de notificación webhook MP",           "URL",     true,  false),

                // ── SEGURIDAD ─────────────────────────────────────────────────────
                cfg("SEG_MAX_INTENTOS_LOGIN",  "5",                   CategoriaConfig.SEGURIDAD,"Intentos máximos de login antes de alerta","NUMBER",  true,  false),
                cfg("SEG_MINUTOS_RESET",       "10",                  CategoriaConfig.SEGURIDAD,"Minutos para reset de intentos fallidos",  "NUMBER",  true,  false),
                cfg("SEG_JWT_EXPIRACION_HORAS","24",                  CategoriaConfig.SEGURIDAD,"Tiempo de expiración del JWT (horas)",    "NUMBER",  true,  false),
                cfg("SEG_REGISTRO_PUBLICO",    "true",                CategoriaConfig.SEGURIDAD,"Permitir registro público de usuarios",   "BOOLEAN", true,  false),

                // ── NOTIFICACIONES ────────────────────────────────────────────────
                cfg("NOTIF_EMAIL_REMITENTE",   "noreply@eventperu.com", CategoriaConfig.NOTIFICACIONES, "Email remitente de notificaciones", "EMAIL",  true,  false),
                cfg("NOTIF_COMPRA_HABILITADA", "true",                CategoriaConfig.NOTIFICACIONES, "Notificar nuevas compras",           "BOOLEAN", true,  false),
                cfg("NOTIF_CANCELACION_HABILITADA","true",            CategoriaConfig.NOTIFICACIONES, "Notificar cancelaciones",            "BOOLEAN", true,  false),
                cfg("NOTIF_ALERTAS_AUDIT",     "true",                CategoriaConfig.NOTIFICACIONES, "Notificar alertas de auditoría",     "BOOLEAN", true,  false),

                // ── CUPONES ───────────────────────────────────────────────────────
                cfg("CUPONES_DESCUENTO_MAX_PCT","50",                 CategoriaConfig.CUPONES,  "Descuento máximo global permitido (%)",   "NUMBER",  true,  false),
                cfg("CUPONES_USOS_DEFAULT",    "100",                 CategoriaConfig.CUPONES,  "Usos máximos por defecto al crear cupón", "NUMBER",  true,  false)
        );

        repo.saveAll(defaults);
        log.info("Configuración inicializada con {} registros", defaults.size());
    }

    private ConfiguracionModel cfg(String clave, String valor, CategoriaConfig categoria,
                                   String descripcion, String tipo, boolean editable, boolean sensible) {
        return ConfiguracionModel.builder()
                .clave(clave).valor(valor).categoria(categoria)
                .descripcion(descripcion).tipo(tipo)
                .editable(editable).sensible(sensible)
                .build();
    }
}
