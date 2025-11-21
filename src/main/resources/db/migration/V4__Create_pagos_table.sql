-- V4__Create_pagos_table.sql
-- Migración: Tabla de pagos

CREATE TABLE IF NOT EXISTS pagos (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL UNIQUE,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    id_transaccion_externa VARCHAR(255) UNIQUE,
    fecha_procesamiento TIMESTAMP,
    fecha_liberacion TIMESTAMP,
    fecha_retencion TIMESTAMP,
    comision_plataforma DECIMAL(10,2) NOT NULL,
    monto_anfitrion DECIMAL(10,2),
    detalles TEXT,
    motivo_reembolso TEXT,
    fecha_reembolso TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_reserva_pago FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE RESTRICT,
    CONSTRAINT chk_metodo_pago CHECK (metodo_pago IN ('TARJETA_CREDITO', 'TARJETA_DEBITO', 'PAYPAL', 'STRIPE', 'TRANSFERENCIA_BANCARIA')),
    CONSTRAINT chk_estado_pago CHECK (estado IN ('PENDIENTE', 'PROCESANDO', 'COMPLETADO', 'RETENIDO', 'LIBERADO', 'FALLIDO', 'REEMBOLSADO', 'DISPUTADO')),
    CONSTRAINT chk_monto CHECK (monto > 0)
);

-- Índices
CREATE INDEX idx_reserva_pago ON pagos(reserva_id);
CREATE INDEX idx_estado_pago ON pagos(estado);
CREATE INDEX idx_transaccion ON pagos(id_transaccion_externa);
CREATE INDEX idx_fecha_procesamiento ON pagos(fecha_procesamiento);

COMMENT ON TABLE pagos IS 'Tabla de pagos procesados por las reservas';
COMMENT ON COLUMN pagos.id_transaccion_externa IS 'ID de la transacción del proveedor de pagos (Stripe, PayPal)';
