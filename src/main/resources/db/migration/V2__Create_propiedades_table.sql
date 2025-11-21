-- V2__Create_propiedades_table.sql
-- Migración: Tabla de propiedades

CREATE TABLE IF NOT EXISTS propiedades (
    id BIGSERIAL PRIMARY KEY,
    anfitrion_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    tipo_propiedad VARCHAR(30) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    habitaciones INTEGER NOT NULL,
    camas INTEGER NOT NULL,
    banos INTEGER NOT NULL,
    capacidad INTEGER NOT NULL,
    area_m2 INTEGER,
    precio_por_noche DECIMAL(10,2) NOT NULL,
    precio_limpieza DECIMAL(10,2) DEFAULT 0.00,
    deposito_seguridad DECIMAL(10,2) DEFAULT 0.00,
    reglas_casa TEXT,
    hora_checkin VARCHAR(20),
    hora_checkout VARCHAR(20),
    estancia_minima INTEGER DEFAULT 1,
    estancia_maxima INTEGER,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_APROBACION',
    puntuacion_promedio DOUBLE PRECISION DEFAULT 0.0,
    total_valoraciones INTEGER DEFAULT 0,
    total_reservas INTEGER DEFAULT 0,
    verificacion_completa BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    CONSTRAINT fk_anfitrion FOREIGN KEY (anfitrion_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_tipo_propiedad CHECK (tipo_propiedad IN ('CASA_COMPLETA', 'APARTAMENTO', 'HABITACION_PRIVADA', 'HABITACION_COMPARTIDA', 'ESTUDIO', 'LOFT', 'CABANA', 'VILLA', 'OTRO')),
    CONSTRAINT chk_estado_propiedad CHECK (estado IN ('ACTIVA', 'INACTIVA', 'PENDIENTE_APROBACION', 'BLOQUEADA', 'ELIMINADA')),
    CONSTRAINT chk_habitaciones CHECK (habitaciones > 0),
    CONSTRAINT chk_camas CHECK (camas > 0),
    CONSTRAINT chk_banos CHECK (banos > 0),
    CONSTRAINT chk_capacidad CHECK (capacidad > 0),
    CONSTRAINT chk_precio CHECK (precio_por_noche > 0),
    CONSTRAINT chk_puntuacion_propiedad CHECK (puntuacion_promedio >= 0 AND puntuacion_promedio <= 5)
);

-- Índices para optimización
CREATE INDEX idx_anfitrion ON propiedades(anfitrion_id);
CREATE INDEX idx_estado_propiedad ON propiedades(estado);
CREATE INDEX idx_ciudad ON propiedades(ciudad);
CREATE INDEX idx_precio ON propiedades(precio_por_noche);
CREATE INDEX idx_tipo_propiedad ON propiedades(tipo_propiedad);
CREATE INDEX idx_capacidad ON propiedades(capacidad);
CREATE INDEX idx_puntuacion ON propiedades(puntuacion_promedio);

-- Tabla para servicios de la propiedad
CREATE TABLE IF NOT EXISTS propiedad_servicios (
    propiedad_id BIGINT NOT NULL,
    servicio VARCHAR(100) NOT NULL,
    
    CONSTRAINT fk_propiedad_servicio FOREIGN KEY (propiedad_id) REFERENCES propiedades(id) ON DELETE CASCADE,
    PRIMARY KEY (propiedad_id, servicio)
);

-- Tabla para imágenes de la propiedad
CREATE TABLE IF NOT EXISTS propiedad_imagenes (
    propiedad_id BIGINT NOT NULL,
    url_imagen VARCHAR(500) NOT NULL,
    orden INTEGER DEFAULT 0,
    
    CONSTRAINT fk_propiedad_imagen FOREIGN KEY (propiedad_id) REFERENCES propiedades(id) ON DELETE CASCADE,
    PRIMARY KEY (propiedad_id, url_imagen)
);

CREATE INDEX idx_propiedad_servicios ON propiedad_servicios(propiedad_id);
CREATE INDEX idx_propiedad_imagenes ON propiedad_imagenes(propiedad_id);

COMMENT ON TABLE propiedades IS 'Tabla de propiedades/alojamientos publicados por anfitriones';
