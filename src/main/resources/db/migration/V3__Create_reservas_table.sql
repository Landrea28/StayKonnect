-- V3__Create_reservas_table.sql
-- Migración: Tabla de reservas

CREATE TABLE IF NOT EXISTS reservas (
    id BIGSERIAL PRIMARY KEY,
    viajero_id BIGINT NOT NULL,
    propiedad_id BIGINT NOT NULL,
    codigo_reserva VARCHAR(20) NOT NULL UNIQUE,
    fecha_checkin DATE NOT NULL,
    fecha_checkout DATE NOT NULL,
    numero_huespedes INTEGER NOT NULL,
    checkin_realizado TIMESTAMP,
    checkout_realizado TIMESTAMP,
    precio_por_noche DECIMAL(10,2) NOT NULL,
    numero_noches INTEGER NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    precio_limpieza DECIMAL(10,2) DEFAULT 0.00,
    comision_plataforma DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    notas TEXT,
    motivo_cancelacion TEXT,
    fecha_cancelacion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_viajero FOREIGN KEY (viajero_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
    CONSTRAINT fk_propiedad_reserva FOREIGN KEY (propiedad_id) REFERENCES propiedades(id) ON DELETE RESTRICT,
    CONSTRAINT chk_estado_reserva CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA', 'CANCELADA', 'RECHAZADA')),
    CONSTRAINT chk_fechas_reserva CHECK (fecha_checkout > fecha_checkin),
    CONSTRAINT chk_huespedes CHECK (numero_huespedes > 0),
    CONSTRAINT chk_noches CHECK (numero_noches > 0)
);

-- Índices para optimización
CREATE INDEX idx_viajero ON reservas(viajero_id);
CREATE INDEX idx_propiedad_reserva ON reservas(propiedad_id);
CREATE INDEX idx_estado_reserva ON reservas(estado);
CREATE INDEX idx_fechas ON reservas(fecha_checkin, fecha_checkout);
CREATE INDEX idx_codigo_reserva ON reservas(codigo_reserva);

COMMENT ON TABLE reservas IS 'Tabla de reservas realizadas por viajeros';
COMMENT ON COLUMN reservas.codigo_reserva IS 'Código único de la reserva para identificación';
