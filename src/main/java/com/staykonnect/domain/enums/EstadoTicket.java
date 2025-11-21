package com.staykonnect.domain.enums;

/**
 * Estados de un ticket de soporte
 */
public enum EstadoTicket {
    ABIERTO,        // Ticket creado, sin asignar
    ASIGNADO,       // Asignado a un agente
    EN_PROCESO,     // En proceso de resolución
    RESUELTO,       // Resuelto por el agente
    CERRADO,        // Cerrado por el usuario
    REABIERTO       // Reabierto por el usuario
}
