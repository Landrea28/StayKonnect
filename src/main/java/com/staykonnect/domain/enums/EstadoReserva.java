package com.staykonnect.domain.enums;

/**
 * Estados de una reserva
 */
public enum EstadoReserva {
    PENDIENTE,      // Solicitud enviada, esperando confirmación
    CONFIRMADA,     // Confirmada por el anfitrión
    PAGADA,         // Pago procesado exitosamente
    EN_CURSO,       // Check-in realizado, huésped en la propiedad
    COMPLETADA,     // Check-out realizado, estancia finalizada
    CANCELADA,      // Cancelada por usuario o anfitrión
    RECHAZADA       // Rechazada por el anfitrión
}
