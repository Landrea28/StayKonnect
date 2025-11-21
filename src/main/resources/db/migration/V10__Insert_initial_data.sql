-- V10__Insert_initial_data.sql
-- Migración: Datos iniciales para desarrollo y pruebas

-- Usuario administrador (contraseña: Admin123!)
-- Nota: En producción, esta contraseña debe ser hasheada con BCrypt
INSERT INTO usuarios (nombre, apellido, email, password, rol, estado, email_verificado, created_at)
VALUES 
    ('Admin', 'Sistema', 'admin@staykonnect.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 'ACTIVA', true, CURRENT_TIMESTAMP),
    ('Juan', 'Pérez', 'juan.perez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ANFITRION', 'ACTIVA', true, CURRENT_TIMESTAMP),
    ('María', 'García', 'maria.garcia@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIAJERO', 'ACTIVA', true, CURRENT_TIMESTAMP),
    ('Carlos', 'Rodríguez', 'carlos.rodriguez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ANFITRION', 'ACTIVA', true, CURRENT_TIMESTAMP),
    ('Ana', 'Martínez', 'ana.martinez@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'VIAJERO', 'ACTIVA', true, CURRENT_TIMESTAMP);

-- Propiedades de ejemplo
INSERT INTO propiedades (
    anfitrion_id, titulo, descripcion, tipo_propiedad, direccion, ciudad, pais,
    habitaciones, camas, banos, capacidad, precio_por_noche, precio_limpieza,
    hora_checkin, hora_checkout, estado, verificacion_completa, created_at
)
VALUES
    (2, 'Apartamento Moderno en Centro', 'Hermoso apartamento totalmente equipado en el corazón de la ciudad. Perfecto para viajeros de negocios o turismo.', 
     'APARTAMENTO', 'Calle Principal 123', 'Bogotá', 'Colombia', 2, 2, 1, 4, 150000.00, 30000.00,
     '14:00', '11:00', 'ACTIVA', true, CURRENT_TIMESTAMP),
    
    (2, 'Casa de Playa con Vista al Mar', 'Casa completa frente al mar con todas las comodidades. Ideal para familias.', 
     'CASA_COMPLETA', 'Avenida del Mar 456', 'Cartagena', 'Colombia', 4, 6, 3, 8, 450000.00, 80000.00,
     '15:00', '10:00', 'ACTIVA', true, CURRENT_TIMESTAMP),
    
    (4, 'Habitación Privada Acogedora', 'Habitación privada en casa familiar, ambiente tranquilo y seguro.', 
     'HABITACION_PRIVADA', 'Carrera 7 # 89-12', 'Medellín', 'Colombia', 1, 1, 1, 2, 80000.00, 15000.00,
     '13:00', '11:00', 'ACTIVA', true, CURRENT_TIMESTAMP),
    
    (4, 'Loft Industrial en Zona Artística', 'Espacio único de estilo industrial en el barrio artístico de la ciudad.', 
     'LOFT', 'Calle Bohemia 34', 'Bogotá', 'Colombia', 1, 2, 1, 3, 200000.00, 40000.00,
     '14:00', '12:00', 'ACTIVA', true, CURRENT_TIMESTAMP);

-- Servicios para las propiedades
INSERT INTO propiedad_servicios (propiedad_id, servicio)
VALUES
    (1, 'WiFi'), (1, 'Cocina'), (1, 'Aire Acondicionado'), (1, 'TV'), (1, 'Estacionamiento'),
    (2, 'WiFi'), (2, 'Cocina'), (2, 'Aire Acondicionado'), (2, 'Piscina'), (2, 'Playa'), (2, 'Estacionamiento'),
    (3, 'WiFi'), (3, 'Desayuno'), (3, 'Aire Acondicionado'),
    (4, 'WiFi'), (4, 'Cocina'), (4, 'Calefacción'), (4, 'TV'), (4, 'Lavadora');

-- Imágenes de ejemplo para las propiedades
INSERT INTO propiedad_imagenes (propiedad_id, url_imagen, orden)
VALUES
    (1, '/images/propiedades/apt1_1.jpg', 1),
    (1, '/images/propiedades/apt1_2.jpg', 2),
    (2, '/images/propiedades/casa1_1.jpg', 1),
    (2, '/images/propiedades/casa1_2.jpg', 2),
    (3, '/images/propiedades/hab1_1.jpg', 1),
    (4, '/images/propiedades/loft1_1.jpg', 1);

-- Reserva de ejemplo
INSERT INTO reservas (
    viajero_id, propiedad_id, codigo_reserva, fecha_checkin, fecha_checkout,
    numero_huespedes, precio_por_noche, numero_noches, subtotal, comision_plataforma, total,
    estado, created_at
)
VALUES
    (3, 1, 'RES-2025-000001', '2025-12-01', '2025-12-05', 2, 150000.00, 4, 600000.00, 60000.00, 690000.00, 'CONFIRMADA', CURRENT_TIMESTAMP),
    (5, 2, 'RES-2025-000002', '2025-12-15', '2025-12-20', 6, 450000.00, 5, 2250000.00, 225000.00, 2555000.00, 'PAGADA', CURRENT_TIMESTAMP);

COMMENT ON TABLE usuarios IS 'Datos iniciales de usuarios para desarrollo. Contraseña para todos: Admin123!';
