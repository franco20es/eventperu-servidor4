package com.example.AUHT_SERVICE.MODEL;

public enum CategoriaAudit {
    ACTIVIDAD,       // acciones normales del sistema
    SESION,          // login, logout, token expirado
    NO_AUTORIZADO,   // 403, acceso sin rol, endpoint bloqueado
    CAMBIO_CRITICO,  // crear admin/staff, eliminar, cambiar estado
    ALERTA           // intentos fallidos repetidos, doble uso de ticket, etc.
}

