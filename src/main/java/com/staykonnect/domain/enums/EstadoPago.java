package com.staykonnect.domain.enums;

/**
 * Estados de un pago
 */
public enum EstadoPago {
    PENDIENTE,      // Pago iniciado pero no procesado
    PROCESANDO,     // En proceso de validación
    COMPLETADO,     // Pago exitoso
    RETENIDO,       // Retenido 24h después del check-in
    LIBERADO,       // Liberado al anfitrión
    FALLIDO,        // Pago fallido
    REEMBOLSADO,    // Reembolsado al viajero
    DISPUTADO       // En disputa
}
