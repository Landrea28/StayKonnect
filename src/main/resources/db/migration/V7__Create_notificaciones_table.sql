-- V7__Create_notificaciones_table.sql
-- Migración: Tabla de notificaciones

CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_lectura TIMESTAMP,
    enviada_email BOOLEAN DEFAULT FALSE,
    enviada_push BOOLEAN DEFAULT FALSE,
    enlace VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_usuario_notificacion FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_tipo_notificacion CHECK (tipo IN (
        'RESERVA_NUEVA', 'RESERVA_CONFIRMADA', 'RESERVA_CANCELADA',
        'PAGO_RECIBIDO', 'PAGO_LIBERADO', 'MENSAJE_NUEVO',
        'VALORACION_NUEVA', 'RECORDATORIO_CHECKIN', 'RECORDATORIO_CHECKOUT',
        'ACTUALIZACION_SISTEMA', 'ALERTA_SEGURIDAD'
    ))
);

-- Índices
CREATE INDEX idx_usuario_notificacion ON notificaciones(usuario_id);
CREATE INDEX idx_tipo_notificacion ON notificaciones(tipo);
CREATE INDEX idx_leida ON notificaciones(leida);
CREATE INDEX idx_fecha_notificacion ON notificaciones(created_at);

COMMENT ON TABLE notificaciones IS 'Tabla de notificaciones enviadas a usuarios';
