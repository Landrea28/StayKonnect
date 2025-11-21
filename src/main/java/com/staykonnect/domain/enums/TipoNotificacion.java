package com.staykonnect.domain.enums;

/**
 * Tipos de notificaciones del sistema
 */
public enum TipoNotificacion {
    // Autenticación
    REGISTRO_EXITOSO,
    EMAIL_VERIFICADO,
    PASSWORD_RECUPERADA,
    
    // Reservas
    RESERVA_CREADA,
    RESERVA_CONFIRMADA,
    RESERVA_RECHAZADA,
    RESERVA_CANCELADA,
    RESERVA_CHECKIN_HOY,
    RESERVA_CHECKOUT_HOY,
    RESERVA_COMPLETADA,
    
    // Pagos
    PAGO_EXITOSO,
    PAGO_FALLIDO,
    PAGO_REEMBOLSADO,
    PAGO_PENDIENTE,
    
    // Mensajes
    MENSAJE_NUEVO,
    
    // Valoraciones
    VALORACION_NUEVA,
    VALORACION_RESPONDIDA,
    
    // Admin
    USUARIO_BANEADO,
    USUARIO_DESBANEADO,
    PROPIEDAD_APROBADA,
    PROPIEDAD_RECHAZADA,
    RESERVA_CANCELADA_ADMIN,
    VALORACION_OCULTA,
    
    // Sistema
    SISTEMA_MANTENIMIENTO,
    SISTEMA_ALERTA
}
