-- V1__Create_usuarios_table.sql
-- Migración inicial: Tabla de usuarios

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    biografia TEXT,
    foto_perfil VARCHAR(255),
    rol VARCHAR(20) NOT NULL DEFAULT 'VIAJERO',
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_VERIFICACION',
    email_verificado BOOLEAN DEFAULT FALSE,
    token_verificacion VARCHAR(255),
    token_recuperacion VARCHAR(255),
    puntuacion_promedio DOUBLE PRECISION DEFAULT 0.0,
    total_valoraciones INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT chk_rol CHECK (rol IN ('VIAJERO', 'ANFITRION', 'ADMIN')),
    CONSTRAINT chk_estado CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA', 'PENDIENTE_VERIFICACION')),
    CONSTRAINT chk_puntuacion CHECK (puntuacion_promedio >= 0 AND puntuacion_promedio <= 5)
);

-- Índices para optimización de consultas
CREATE INDEX idx_email ON usuarios(email);
CREATE INDEX idx_estado ON usuarios(estado);
CREATE INDEX idx_rol ON usuarios(rol);
CREATE INDEX idx_email_verificado ON usuarios(email_verificado);

-- Comentarios
COMMENT ON TABLE usuarios IS 'Tabla principal de usuarios del sistema (Viajeros, Anfitriones, Administradores)';
COMMENT ON COLUMN usuarios.rol IS 'Rol del usuario: VIAJERO, ANFITRION, ADMIN';
COMMENT ON COLUMN usuarios.estado IS 'Estado de la cuenta: ACTIVA, INACTIVA, BLOQUEADA, PENDIENTE_VERIFICACION';
