# Script de inicialización de base de datos PostgreSQL
# Este script crea la base de datos y configura permisos

-- Crear base de datos de desarrollo
CREATE DATABASE staykonnect_dev;

-- Crear base de datos de pruebas
CREATE DATABASE staykonnect_test;

-- Conectar a la base de datos de desarrollo
\c staykonnect_dev;

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- Para búsquedas de texto similares

-- Comentario sobre la base de datos
COMMENT ON DATABASE staykonnect_dev IS 'Base de datos de desarrollo para StayKonnect - Plataforma de alojamiento';

-- Información de conexión
\echo 'Base de datos staykonnect_dev creada exitosamente'
\echo 'Usuario: postgres'
\echo 'Puerto: 5432'
\echo ''
\echo 'Extensiones instaladas:'
\dx
