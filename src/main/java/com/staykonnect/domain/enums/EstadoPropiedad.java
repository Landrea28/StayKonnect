package com.staykonnect.domain.enums;

/**
 * Estados de una propiedad
 */
public enum EstadoPropiedad {
    ACTIVA,               // Propiedad publicada y disponible
    INACTIVA,             // Propiedad desactivada temporalmente
    PENDIENTE_APROBACION, // Pendiente de aprobación por admin
    BLOQUEADA,            // Bloqueada por incumplimiento
    ELIMINADA             // Eliminada (soft delete)
}
