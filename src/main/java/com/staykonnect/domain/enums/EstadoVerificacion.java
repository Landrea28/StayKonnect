package com.staykonnect.domain.enums;

/**
 * Estados de verificación de documentos
 */
public enum EstadoVerificacion {
    PENDIENTE,      // Documento subido, pendiente de revisión
    EN_REVISION,    // En proceso de revisión
    APROBADO,       // Documento aprobado
    RECHAZADO       // Documento rechazado
}
