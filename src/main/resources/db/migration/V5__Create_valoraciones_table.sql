-- V5__Create_valoraciones_table.sql
-- Migración: Tabla de valoraciones

CREATE TABLE IF NOT EXISTS valoraciones (
    id BIGSERIAL PRIMARY KEY,
    evaluador_id BIGINT NOT NULL,
    evaluado_id BIGINT NOT NULL,
    propiedad_id BIGINT,
    reserva_id BIGINT NOT NULL UNIQUE,
    puntuacion INTEGER NOT NULL,
    comentario TEXT NOT NULL,
    limpieza INTEGER NOT NULL DEFAULT 0,
    comunicacion INTEGER NOT NULL DEFAULT 0,
    ubicacion INTEGER NOT NULL DEFAULT 0,
    valor_por_dinero INTEGER NOT NULL DEFAULT 0,
    respuesta_anfitrion TEXT,
    es_visible BOOLEAN DEFAULT TRUE,
    reportado BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_evaluador FOREIGN KEY (evaluador_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_evaluado FOREIGN KEY (evaluado_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_propiedad_valoracion FOREIGN KEY (propiedad_id) REFERENCES propiedades(id) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_valoracion FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE,
    CONSTRAINT chk_puntuacion_valoracion CHECK (puntuacion >= 1 AND puntuacion <= 5),
    CONSTRAINT chk_limpieza CHECK (limpieza >= 0 AND limpieza <= 5),
    CONSTRAINT chk_comunicacion CHECK (comunicacion >= 0 AND comunicacion <= 5),
    CONSTRAINT chk_ubicacion CHECK (ubicacion >= 0 AND ubicacion <= 5),
    CONSTRAINT chk_valor_dinero CHECK (valor_por_dinero >= 0 AND valor_por_dinero <= 5)
);

-- Índices
CREATE INDEX idx_evaluador ON valoraciones(evaluador_id);
CREATE INDEX idx_evaluado ON valoraciones(evaluado_id);
CREATE INDEX idx_propiedad_valoracion ON valoraciones(propiedad_id);
CREATE INDEX idx_reserva_valoracion ON valoraciones(reserva_id);
CREATE INDEX idx_es_visible ON valoraciones(es_visible);

COMMENT ON TABLE valoraciones IS 'Tabla de valoraciones bidireccionales entre usuarios';
