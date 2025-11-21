-- Crear tabla de valoraciones para el sistema de reputación
CREATE TABLE IF NOT EXISTS valoracion (
    id BIGSERIAL PRIMARY KEY,
    puntuacion INTEGER NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
    comentario TEXT,
    respuesta_anfitrion TEXT,
    fecha_respuesta TIMESTAMP,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Relaciones
    reserva_id BIGINT NOT NULL UNIQUE, -- Una valoración por reserva
    valorador_id BIGINT NOT NULL, -- Usuario que valora
    valorado_id BIGINT NOT NULL, -- Usuario valorado
    propiedad_id BIGINT NOT NULL, -- Propiedad valorada
    
    -- Auditoría
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_valoracion_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id),
    CONSTRAINT fk_valoracion_valorador FOREIGN KEY (valorador_id) REFERENCES usuario(id),
    CONSTRAINT fk_valoracion_valorado FOREIGN KEY (valorado_id) REFERENCES usuario(id),
    CONSTRAINT fk_valoracion_propiedad FOREIGN KEY (propiedad_id) REFERENCES propiedad(id)
);

-- Índices para optimizar consultas
CREATE INDEX idx_valoracion_reserva ON valoracion(reserva_id);
CREATE INDEX idx_valoracion_valorador ON valoracion(valorador_id);
CREATE INDEX idx_valoracion_valorado ON valoracion(valorado_id);
CREATE INDEX idx_valoracion_propiedad ON valoracion(propiedad_id);
CREATE INDEX idx_valoracion_puntuacion ON valoracion(puntuacion);
CREATE INDEX idx_valoracion_visible ON valoracion(visible) WHERE visible = TRUE;
CREATE INDEX idx_valoracion_created_date ON valoracion(created_date DESC);

-- Índice compuesto para estadísticas de propiedad
CREATE INDEX idx_valoracion_propiedad_visible ON valoracion(propiedad_id, visible) WHERE visible = TRUE;

-- Índice compuesto para valoraciones de usuario
CREATE INDEX idx_valoracion_valorado_visible ON valoracion(valorado_id, visible) WHERE visible = TRUE;

-- Comentarios
COMMENT ON TABLE valoracion IS 'Valoraciones y comentarios de reservas completadas';
COMMENT ON COLUMN valoracion.puntuacion IS 'Puntuación de 1 a 5 estrellas';
COMMENT ON COLUMN valoracion.comentario IS 'Comentario del huésped sobre su experiencia';
COMMENT ON COLUMN valoracion.respuesta_anfitrion IS 'Respuesta del anfitrión a la valoración';
COMMENT ON COLUMN valoracion.fecha_respuesta IS 'Fecha en que el anfitrión respondió';
COMMENT ON COLUMN valoracion.visible IS 'Si la valoración es visible públicamente (para moderación)';
COMMENT ON COLUMN valoracion.valorador_id IS 'Usuario que realizó la valoración (viajero)';
COMMENT ON COLUMN valoracion.valorado_id IS 'Usuario valorado (anfitrión de la propiedad)';
