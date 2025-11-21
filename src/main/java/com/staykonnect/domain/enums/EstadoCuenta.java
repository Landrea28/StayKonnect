package com.staykonnect.domain.enums;

/**
 * Estados posibles de una cuenta de usuario
 */
public enum EstadoCuenta {
    ACTIVA,           // Cuenta activa y verificada
    INACTIVA,         // Cuenta desactivada temporalmente
    BLOQUEADA,        // Cuenta bloqueada por administrador
    PENDIENTE_VERIFICACION  // Pendiente de verificación de email
}
