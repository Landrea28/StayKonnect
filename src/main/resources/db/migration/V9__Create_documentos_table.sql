-- V9__Create_documentos_table.sql
-- Migración: Tabla de documentos legales

CREATE TABLE IF NOT EXISTS documentos (
    id BIGSERIAL PRIMARY KEY,
    propiedad_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    url_archivo VARCHAR(500) NOT NULL,
    numero_documento VARCHAR(100),
    fecha_emision DATE,
    fecha_vencimiento DATE,
    estado_verificacion VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    observaciones TEXT,
    verificado_por VARCHAR(100),
    fecha_verificacion TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_propiedad_documento FOREIGN KEY (propiedad_id) REFERENCES propiedades(id) ON DELETE CASCADE,
    CONSTRAINT chk_tipo_documento CHECK (tipo IN (
        'LICENCIA_OPERACION', 'REGISTRO_PROPIEDAD', 'CEDULA_CATASTRAL',
        'CERTIFICADO_SEGURIDAD', 'POLIZA_SEGURO', 'PERMISO_TURISMO', 'OTRO'
    )),
    CONSTRAINT chk_estado_verificacion CHECK (estado_verificacion IN ('PENDIENTE', 'EN_REVISION', 'APROBADO', 'RECHAZADO'))
);

-- Índices
CREATE INDEX idx_propiedad_documento ON documentos(propiedad_id);
CREATE INDEX idx_tipo_documento ON documentos(tipo);
CREATE INDEX idx_estado_verificacion ON documentos(estado_verificacion);
CREATE INDEX idx_fecha_vencimiento ON documentos(fecha_vencimiento);

COMMENT ON TABLE documentos IS 'Tabla de documentos legales y de cumplimiento normativo de propiedades';
