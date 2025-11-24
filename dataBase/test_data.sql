-- Script de datos de prueba para StayKonnect

-- 1. Usuarios (1 Admin, 5 Hosts, 5 Travelers)
-- Password hash para 'password123' (ejemplo, en producción usar BCrypt real)
-- Asumiremos que el backend usa BCrypt, aquí pondremos un hash dummy o uno válido si es posible.
-- Para efectos de prueba, insertaremos usuarios con un hash conocido o generaremos uno.
-- Nota: Si el backend usa BCrypt, estos hashes deben ser válidos.
-- Hash para "123456": $2a$10$2.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j.j (Dummy)
-- Usaremos un hash real generado por BCrypt para "123456": $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm

INSERT INTO users (first_name, last_name, email, password_hash, role, is_verified) VALUES
('Admin', 'User', 'admin@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'ADMIN', true),
('Host', 'One', 'host1@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'HOST', true),
('Host', 'Two', 'host2@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'HOST', true),
('Host', 'Three', 'host3@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'HOST', false),
('Host', 'Four', 'host4@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'HOST', true),
('Host', 'Five', 'host5@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'HOST', true),
('Traveler', 'One', 'traveler1@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'TRAVELER', true),
('Traveler', 'Two', 'traveler2@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'TRAVELER', true),
('Traveler', 'Three', 'traveler3@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'TRAVELER', false),
('Traveler', 'Four', 'traveler4@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'TRAVELER', true),
('Traveler', 'Five', 'traveler5@staykonnect.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.g/j.eAm', 'TRAVELER', true);

-- 2. Propiedades (10 propiedades asignadas a los hosts)
INSERT INTO properties (host_id, title, description, address, city, country, price_per_night, max_guests, legal_status, is_active) VALUES
(2, 'Cozy Apartment in Paris', 'Beautiful apartment near Eiffel Tower', '123 Rue de Paris', 'Paris', 'France', 150.00, 2, 'VERIFIED', true),
(2, 'Modern Loft in NY', 'Spacious loft in Manhattan', '456 5th Ave', 'New York', 'USA', 300.00, 4, 'VERIFIED', true),
(3, 'Beach House in Cancun', 'Relaxing house by the sea', '789 Ocean Blvd', 'Cancun', 'Mexico', 200.00, 6, 'VERIFIED', true),
(3, 'Mountain Cabin in Aspen', 'Cozy cabin for skiing', '101 Snow Way', 'Aspen', 'USA', 250.00, 5, 'PENDING', true),
(4, 'City Center Flat in London', 'Close to all attractions', '202 Baker St', 'London', 'UK', 180.00, 3, 'VERIFIED', true),
(4, 'Historic Villa in Rome', 'Experience ancient Rome', '303 Via Roma', 'Rome', 'Italy', 220.00, 4, 'VERIFIED', true),
(5, 'Modern Condo in Tokyo', 'High-tech condo in Shibuya', '404 Shibuya Crossing', 'Tokyo', 'Japan', 190.00, 2, 'PENDING', true),
(5, 'Seaside Villa in Sydney', 'Beautiful views of the opera house', '505 Harbor Dr', 'Sydney', 'Australia', 280.00, 5, 'VERIFIED', true),
(6, 'Rustic Cottage in Cotswolds', 'Charming countryside cottage', '606 Country Ln', 'Cotswolds', 'UK', 160.00, 4, 'VERIFIED', true),
(6, 'Luxury Penthouse in Dubai', 'Top floor with amazing views', '707 Sheikh Zayed Rd', 'Dubai', 'UAE', 500.00, 2, 'VERIFIED', true);

-- 3. Reservas (10 reservas)
INSERT INTO reservations (guest_id, property_id, start_date, end_date, total_price, status) VALUES
(7, 1, '2025-06-01', '2025-06-05', 600.00, 'CONFIRMED'),
(8, 2, '2025-07-10', '2025-07-15', 1500.00, 'PENDING'),
(9, 3, '2025-08-01', '2025-08-07', 1200.00, 'CONFIRMED'),
(10, 4, '2025-12-20', '2025-12-27', 1750.00, 'PENDING'),
(11, 5, '2025-05-05', '2025-05-10', 900.00, 'CANCELLED'),
(7, 6, '2025-09-15', '2025-09-20', 1100.00, 'CONFIRMED'),
(8, 7, '2025-10-01', '2025-10-05', 760.00, 'PENDING'),
(9, 8, '2025-11-10', '2025-11-15', 1400.00, 'CONFIRMED'),
(10, 9, '2025-06-15', '2025-06-20', 800.00, 'PENDING'),
(11, 10, '2025-12-30', '2026-01-05', 3000.00, 'CONFIRMED');

-- 4. Notificaciones (Algunas de prueba)
INSERT INTO notifications (user_id, message, is_read) VALUES
(2, 'New reservation for your Paris apartment', false),
(7, 'Your reservation in Paris is confirmed', true),
(3, 'New reservation for your Cancun house', false),
(9, 'Your reservation in Cancun is confirmed', false),
(1, 'New user registration: Host Five', true);
