# Scripts de Base de Datos - StayKonnect

Este directorio contiene todos los scripts de migración de base de datos usando Flyway.

## Estructura de Migraciones

Las migraciones siguen el patrón de nomenclatura de Flyway:
- `V{version}__{description}.sql`
- Ejemplo: `V1__Create_usuarios_table.sql`

## Orden de Migraciones

1. **V1** - Tabla `usuarios` (Usuarios del sistema)
2. **V2** - Tabla `propiedades` + tablas relacionadas (servicios, imágenes)
3. **V3** - Tabla `reservas` (Reservas de propiedades)
4. **V4** - Tabla `pagos` (Pagos de reservas)
5. **V5** - Tabla `valoraciones` (Sistema de reputación)
6. **V6** - Tabla `mensajes` (Mensajería interna)
7. **V7** - Tabla `notificaciones` (Sistema de notificaciones)
8. **V8** - Tabla `tickets` (Soporte y disputas)
9. **V9** - Tabla `documentos` (Cumplimiento normativo)
10. **V10** - Datos iniciales (Usuarios y propiedades de prueba)

## Configuración de Base de Datos

### Desarrollo Local con Docker

```bash
# Iniciar PostgreSQL con Docker Compose
docker-compose up -d postgres

# Verificar que está corriendo
docker ps

# Conectar a PostgreSQL
docker exec -it staykonnect-postgres psql -U postgres -d staykonnect_dev
```

### Instalación Manual de PostgreSQL

Si prefieres instalar PostgreSQL localmente:

1. Descargar PostgreSQL 15 desde https://www.postgresql.org/download/
2. Crear la base de datos:
```sql
CREATE DATABASE staykonnect_dev;
```

3. Ejecutar el script de inicialización:
```bash
psql -U postgres -d staykonnect_dev -f src/main/resources/db/init-db.sql
```

## Ejecución de Migraciones

Flyway se ejecuta automáticamente al iniciar Spring Boot. También puedes ejecutar manualmente:

```bash
# Maven
mvn flyway:migrate

# Limpiar y recrear (CUIDADO: Elimina todos los datos)
mvn flyway:clean flyway:migrate

# Ver información de migraciones
mvn flyway:info
```

## Datos de Prueba

El script `V10__Insert_initial_data.sql` incluye:

### Usuarios de Prueba
- **Admin**: admin@staykonnect.com / Admin123!
- **Anfitrión 1**: juan.perez@email.com / Admin123!
- **Viajero 1**: maria.garcia@email.com / Admin123!
- **Anfitrión 2**: carlos.rodriguez@email.com / Admin123!
- **Viajero 2**: ana.martinez@email.com / Admin123!

### Propiedades de Ejemplo
- 4 propiedades en diferentes ciudades de Colombia
- Con servicios e imágenes configuradas
- Estados: ACTIVA y verificadas

### Reservas de Ejemplo
- 2 reservas con diferentes estados

## Índices Creados

Todos los índices están optimizados para:
- Búsquedas frecuentes (email, ciudad, fechas)
- Relaciones entre tablas (foreign keys)
- Consultas de estado y filtrado
- Ordenamiento y paginación

## Restricciones (Constraints)

- **Primary Keys**: ID autoincremental en todas las tablas
- **Foreign Keys**: Relaciones con DELETE CASCADE o RESTRICT
- **Unique Constraints**: Email, códigos de reserva, etc.
- **Check Constraints**: Validación de enums, rangos numéricos
- **Not Null**: Campos obligatorios

## Conexión con pgAdmin

Si usas Docker Compose, puedes acceder a pgAdmin:
- URL: http://localhost:5050
- Email: admin@staykonnect.com
- Password: admin123

Agregar servidor PostgreSQL:
- Host: postgres
- Port: 5432
- Database: staykonnect_dev
- Username: postgres
- Password: postgres

## Troubleshooting

### Error: "Database does not exist"
```bash
docker-compose down -v
docker-compose up -d postgres
# Esperar 10 segundos para que inicie
mvn spring-boot:run
```

### Error: "Flyway validation failed"
```bash
# Limpiar migraciones (elimina datos)
mvn flyway:clean
mvn spring-boot:run
```

### Ver logs de PostgreSQL
```bash
docker logs staykonnect-postgres
```

## Backup y Restore

### Crear backup
```bash
docker exec staykonnect-postgres pg_dump -U postgres staykonnect_dev > backup.sql
```

### Restaurar backup
```bash
cat backup.sql | docker exec -i staykonnect-postgres psql -U postgres -d staykonnect_dev
```
