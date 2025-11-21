-- V13: Agregar características de administración
-- Fecha: 2024-11-19
-- Descripción: Campos para baneo de usuarios, aprobación de propiedades, y moderación

-- Agregar campos de administración a usuario
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS baneado BOOLEAN DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS fecha_baneo TIMESTAMP;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS razon_baneo TEXT;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS baneado_por_id BIGINT REFERENCES usuario(id);

-- Agregar campos de aprobación a propiedad
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS aprobada BOOLEAN DEFAULT FALSE;
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS fecha_aprobacion TIMESTAMP;
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS aprobada_por_id BIGINT REFERENCES usuario(id);
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS rechazada BOOLEAN DEFAULT FALSE;
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS fecha_rechazo TIMESTAMP;
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS razon_rechazo TEXT;
ALTER TABLE propiedad ADD COLUMN IF NOT EXISTS rechazada_por_id BIGINT REFERENCES usuario(id);

-- Índices para consultas de administración
CREATE INDEX IF NOT EXISTS idx_usuario_baneado ON usuario(baneado) WHERE baneado = TRUE;
CREATE INDEX IF NOT EXISTS idx_usuario_fecha_baneo ON usuario(fecha_baneo) WHERE fecha_baneo IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_propiedad_aprobada ON propiedad(aprobada);
CREATE INDEX IF NOT EXISTS idx_propiedad_rechazada ON propiedad(rechazada) WHERE rechazada = TRUE;
CREATE INDEX IF NOT EXISTS idx_propiedad_pendiente_aprobacion ON propiedad(aprobada, rechazada) WHERE aprobada = FALSE AND rechazada = FALSE;

-- Comentarios
COMMENT ON COLUMN usuario.baneado IS 'Indica si el usuario está baneado del sistema';
COMMENT ON COLUMN usuario.fecha_baneo IS 'Fecha en que se baneó al usuario';
COMMENT ON COLUMN usuario.razon_baneo IS 'Razón del baneo del usuario';
COMMENT ON COLUMN usuario.baneado_por_id IS 'ID del admin que baneó al usuario';

COMMENT ON COLUMN propiedad.aprobada IS 'Indica si la propiedad fue aprobada por un admin';
COMMENT ON COLUMN propiedad.fecha_aprobacion IS 'Fecha en que se aprobó la propiedad';
COMMENT ON COLUMN propiedad.aprobada_por_id IS 'ID del admin que aprobó la propiedad';
COMMENT ON COLUMN propiedad.rechazada IS 'Indica si la propiedad fue rechazada por un admin';
COMMENT ON COLUMN propiedad.fecha_rechazo IS 'Fecha en que se rechazó la propiedad';
COMMENT ON COLUMN propiedad.razon_rechazo IS 'Razón del rechazo de la propiedad';
COMMENT ON COLUMN propiedad.rechazada_por_id IS 'ID del admin que rechazó la propiedad';
