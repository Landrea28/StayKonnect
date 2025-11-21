-- Crear tabla de mensajes para chat en tiempo real
CREATE TABLE IF NOT EXISTS mensaje (
    id BIGSERIAL PRIMARY KEY,
    contenido TEXT NOT NULL,
    leido BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_lectura TIMESTAMP,
    
    -- Relaciones
    remitente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    reserva_id BIGINT,
    
    -- Auditoría
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_mensaje_remitente FOREIGN KEY (remitente_id) REFERENCES usuario(id),
    CONSTRAINT fk_mensaje_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuario(id),
    CONSTRAINT fk_mensaje_reserva FOREIGN KEY (reserva_id) REFERENCES reserva(id)
);

-- Índices para optimizar consultas
CREATE INDEX idx_mensaje_remitente ON mensaje(remitente_id);
CREATE INDEX idx_mensaje_destinatario ON mensaje(destinatario_id);
CREATE INDEX idx_mensaje_reserva ON mensaje(reserva_id);
CREATE INDEX idx_mensaje_created_date ON mensaje(created_date DESC);
CREATE INDEX idx_mensaje_leido ON mensaje(leido) WHERE leido = FALSE;

-- Índice compuesto para conversaciones
CREATE INDEX idx_mensaje_conversacion ON mensaje(remitente_id, destinatario_id, created_date DESC);

-- Comentarios
COMMENT ON TABLE mensaje IS 'Mensajes entre usuarios del sistema (viajeros y anfitriones)';
COMMENT ON COLUMN mensaje.contenido IS 'Contenido del mensaje';
COMMENT ON COLUMN mensaje.leido IS 'Indica si el mensaje fue leído por el destinatario';
COMMENT ON COLUMN mensaje.fecha_lectura IS 'Fecha y hora en que se leyó el mensaje';
COMMENT ON COLUMN mensaje.reserva_id IS 'Reserva asociada al mensaje (opcional)';
