package com.staykonnect.common.exception;

/**
 * Excepción base para errores de negocio en la aplicación
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
