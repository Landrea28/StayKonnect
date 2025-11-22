-- Script de creación de base de datos para StayKonnect
-- Basado en los requerimientos funcionales y reglas de negocio del proyecto.

-- 1. Tabla de Usuarios (RF01, RB02, RF08)
-- Maneja los roles de Anfitrión, Viajero y Administrador.
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('HOST', 'TRAVELER', 'ADMIN')),
    is_verified BOOLEAN DEFAULT FALSE, -- Para RB02: Comunidad confiable
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Propiedades (RF02, RF12)
-- Almacena la información de los alojamientos.
CREATE TABLE properties (
    id SERIAL PRIMARY KEY,
    host_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    max_guests INT NOT NULL,
    legal_status VARCHAR(20) DEFAULT 'PENDING' CHECK (legal_status IN ('PENDING', 'VERIFIED', 'REJECTED')), -- RF12: Cumplimiento normativo
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Imágenes de Propiedades (RF02)
CREATE TABLE property_images (
    id SERIAL PRIMARY KEY,
    property_id INT NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Servicios/Comodidades (RF02, RF03)
-- Ej: Wifi, Cocina, Parqueadero.
CREATE TABLE amenities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Tabla intermedia Propiedades <-> Servicios
CREATE TABLE property_amenities (
    property_id INT NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    amenity_id INT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    PRIMARY KEY (property_id, amenity_id)
);

-- 5. Tabla de Reservas (RF04)
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    property_id INT NOT NULL REFERENCES properties(id),
    guest_id INT NOT NULL REFERENCES users(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_dates CHECK (end_date > start_date)
);

-- 6. Tabla de Pagos (RF05)
-- Gestiona la retención y liberación de fondos.
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    reservation_id INT NOT NULL REFERENCES reservations(id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'HELD' CHECK (status IN ('HELD', 'RELEASED', 'REFUNDED')), -- HELD: Retenido 24h, RELEASED: Liberado al anfitrión
    transaction_id VARCHAR(100), -- ID de la pasarela de pago (ej. Stripe)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabla de Reseñas (RF07)
-- Bidireccional: Huésped a Propiedad/Anfitrión y Anfitrión a Huésped.
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    reservation_id INT NOT NULL REFERENCES reservations(id),
    author_id INT NOT NULL REFERENCES users(id), -- Quien escribe la reseña
    target_property_id INT REFERENCES properties(id), -- Si se califica la propiedad
    target_user_id INT REFERENCES users(id), -- Si se califica al usuario (ej. anfitrión calificando viajero)
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_target CHECK (
        (target_property_id IS NOT NULL AND target_user_id IS NULL) OR 
        (target_property_id IS NULL AND target_user_id IS NOT NULL)
    )
);

-- 8. Tabla de Mensajes (RF06)
-- Comunicación directa entre usuarios.
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL REFERENCES users(id),
    receiver_id INT NOT NULL REFERENCES users(id),
    reservation_id INT REFERENCES reservations(id), -- Opcional, para contexto
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Tabla de Notificaciones (RF10)
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    type VARCHAR(50) NOT NULL, -- Ej: 'RESERVATION_STATUS', 'PAYMENT', 'MESSAGE'
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. Tabla de Soporte/Disputas (RF11)
CREATE TABLE support_tickets (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    reservation_id INT REFERENCES reservations(id),
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. Tabla de Documentos Legales (RF12)
-- Para validación de propiedades.
CREATE TABLE legal_documents (
    id SERIAL PRIMARY KEY,
    property_id INT NOT NULL REFERENCES properties(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL, -- Ej: 'Escritura', 'Certificado Libertad'
    document_url VARCHAR(500) NOT NULL,
    verification_status VARCHAR(20) DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    reviewed_by INT REFERENCES users(id), -- Admin que revisó
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimizar búsquedas frecuentes (RF03)
CREATE INDEX idx_properties_city ON properties(city);
CREATE INDEX idx_properties_price ON properties(price_per_night);
CREATE INDEX idx_reservations_dates ON reservations(start_date, end_date);
