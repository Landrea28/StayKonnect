-- V8__Create_tickets_table.sql
-- Migración: Tabla de tickets de soporte

CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    reserva_id BIGINT,
    codigo_ticket VARCHAR(20) NOT NULL UNIQUE,
    asunto VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
    prioridad VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    categoria VARCHAR(50),
    fecha_cierre TIMESTAMP,
    tiempo_respuesta_horas INTEGER,
    respuesta TEXT,
    agente_asignado VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_usuario_ticket FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_ticket FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE SET NULL,
    CONSTRAINT chk_estado_ticket CHECK (estado IN ('ABIERTO', 'ASIGNADO', 'EN_PROCESO', 'RESUELTO', 'CERRADO', 'REABIERTO')),
    CONSTRAINT chk_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA', 'URGENTE'))
);

-- Índices
CREATE INDEX idx_usuario_ticket ON tickets(usuario_id);
CREATE INDEX idx_reserva_ticket ON tickets(reserva_id);
CREATE INDEX idx_estado_ticket ON tickets(estado);
CREATE INDEX idx_prioridad ON tickets(prioridad);
CREATE INDEX idx_codigo_ticket ON tickets(codigo_ticket);
CREATE INDEX idx_categoria ON tickets(categoria);

COMMENT ON TABLE tickets IS 'Tabla de tickets de soporte y disputas';
COMMENT ON COLUMN tickets.categoria IS 'CANCELACION, REEMBOLSO, QUEJA, DISPUTA, TECNICO, OTRO';
