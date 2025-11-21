-- V6__Create_mensajes_table.sql
-- Migración: Tabla de mensajes

CREATE TABLE IF NOT EXISTS mensajes (
    id BIGSERIAL PRIMARY KEY,
    remitente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    reserva_id BIGINT,
    contenido TEXT NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    fecha_lectura TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_remitente FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_mensaje FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX idx_remitente ON mensajes(remitente_id);
CREATE INDEX idx_destinatario ON mensajes(destinatario_id);
CREATE INDEX idx_reserva_mensaje ON mensajes(reserva_id);
CREATE INDEX idx_fecha_mensaje ON mensajes(created_at);
CREATE INDEX idx_leido ON mensajes(leido);

COMMENT ON TABLE mensajes IS 'Tabla de mensajes entre usuarios del sistema';
