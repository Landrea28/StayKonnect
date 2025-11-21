# StayKonnect Backend API

Backend de la plataforma StayKonnect - Sistema de alquiler de alojamientos entre particulares.

## Tecnologías

- **Java 21** (LTS)
- **Spring Boot 3.4.1**
- **PostgreSQL** (Base de datos)
- **Spring Security + JWT** (Autenticación)
- **Flyway** (Migraciones de BD)
- **Swagger/OpenAPI** (Documentación)
- **Lombok** (Reducción de código boilerplate)
- **Maven** (Gestión de dependencias)

## Requisitos Previos

- JDK 21
- Maven 3.8+
- PostgreSQL 14+

## Configuración Inicial

### 1. Iniciar la base de datos con Docker (Recomendado)

```bash
# Iniciar PostgreSQL, pgAdmin y MailHog
docker-compose up -d

# Verificar que los servicios están corriendo
docker-compose ps
```

Servicios disponibles:
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050 (admin@staykonnect.com / admin123)
- **MailHog**: http://localhost:8025 (servidor SMTP de prueba)

### 2. Alternativamente: Script de gestión de base de datos

```powershell
# Ejecutar el script de gestión (Windows PowerShell)
.\database-manager.ps1
```

Este script incluye opciones para:
- Iniciar/detener PostgreSQL
- Conectar a la base de datos
- Ejecutar migraciones
- Crear/restaurar backups

### 3. Crear la base de datos manualmente (Opcional)

Si prefieres no usar Docker:

```sql
CREATE DATABASE staykonnect_dev;
```

Configurar `application-dev.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/staykonnect_dev
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

### 5. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

**Nota**: Flyway ejecutará automáticamente las migraciones al iniciar la aplicación.

## Documentación API

Una vez ejecutada la aplicación, la documentación Swagger está disponible en:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Endpoints Disponibles

#### 🔐 Autenticación
- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Inicio de sesión
- `GET /api/auth/verify-email` - Verificación de email
- `POST /api/auth/recover-password` - Recuperación de contraseña
- `POST /api/auth/reset-password` - Reseteo de contraseña

#### 🏠 Propiedades
- `POST /api/propiedades` - Crear propiedad (ANFITRION)
- `GET /api/propiedades/buscar` - Búsqueda avanzada con filtros (público)
- `GET /api/propiedades` - Listar propiedades activas (público)
- `GET /api/propiedades/mis-propiedades` - Mis propiedades (ANFITRION)
- `GET /api/propiedades/{id}` - Detalles de propiedad (público)
- `PUT /api/propiedades/{id}` - Actualizar propiedad (propietario/ADMIN)
- `PATCH /api/propiedades/{id}/estado` - Cambiar estado (propietario/ADMIN)
- `DELETE /api/propiedades/{id}` - Eliminar propiedad (propietario/ADMIN)

#### 📅 Reservas ⭐ NEW
- `POST /api/reservas` - Crear reserva (VIAJERO)
- `PUT /api/reservas/{id}/confirmar` - Confirmar reserva (ANFITRION)
- `PUT /api/reservas/{id}/rechazar` - Rechazar reserva (ANFITRION)
- `PUT /api/reservas/{id}/cancelar` - Cancelar reserva (VIAJERO/ANFITRION)
- `GET /api/reservas/{id}` - Detalles de reserva
- `GET /api/reservas/mis-reservas` - Mis reservas como viajero (VIAJERO)
- `GET /api/reservas/recibidas` - Reservas recibidas (ANFITRION)
- `GET /api/reservas/propiedad/{id}` - Reservas por propiedad (ANFITRION/ADMIN)

#### 📁 Archivos
- `POST /api/files/upload-image` - Subir imagen
- `POST /api/files/upload-images` - Subir múltiples imágenes
- `POST /api/files/upload-document` - Subir documento

### Guías de Testing

- 📘 [Testing de Autenticación](docs/TESTING_AUTH.md)
- 📗 [Testing de Propiedades](docs/TESTING_PROPIEDADES.md)
- 📙 [Testing de Búsqueda y Filtrado](docs/TESTING_BUSQUEDA.md)
- 📕 [Testing de Reservas](docs/TESTING_RESERVAS.md) ⭐ NEW

## Estructura del Proyecto

```
src/main/java/com/staykonnect/
├── config/              # Configuraciones de la aplicación
├── common/              # Clases comunes (DTOs, excepciones)
├── domain/              # Entidades del dominio
│   ├── entity/          # Entidades JPA
│   ├── repository/      # Repositorios
│   └── enums/           # Enumeraciones
├── service/             # Lógica de negocio
├── controller/          # Controladores REST
├── security/            # Configuración de seguridad
└── util/                # Utilidades

src/main/resources/
├── application.properties          # Configuración base
├── application-dev.properties      # Configuración desarrollo
├── application-prod.properties     # Configuración producción
└── db/migration/                   # Scripts Flyway
```

## Funcionalidades Implementadas

### ✅ Fase 1 - Configuración Base
- [x] Configuración Spring Boot con Java 21
- [x] Configuración de base de datos PostgreSQL
- [x] Configuración de perfiles (dev/prod)
- [x] Manejo global de excepciones
- [x] Configuración CORS
- [x] Documentación OpenAPI/Swagger

### ✅ Fase 2 - Modelo de Datos
- [x] 10 Entidades del dominio con validaciones
- [x] 9 Repositorios JPA con queries personalizadas
- [x] Relaciones bidireccionales configuradas
- [x] Auditoría automática con timestamps

### ✅ Fase 3 - Base de Datos
- [x] 10 Scripts de migración Flyway
- [x] Índices optimizados para consultas
- [x] Constraints y validaciones a nivel BD
- [x] Datos iniciales para desarrollo
- [x] Docker Compose con PostgreSQL y pgAdmin

### ✅ Fase 4 - Autenticación y Autorización
- [x] Sistema de JWT con tokens de acceso
- [x] Registro de usuarios (Viajero, Anfitrión, Admin)
- [x] Login con validación de credenciales
- [x] Verificación de email con tokens
- [x] Recuperación y reseteo de contraseña
- [x] Spring Security con filtros personalizados
- [x] Control de acceso basado en roles (RBAC)
- [x] Servicio de email para notificaciones
- [x] DTOs y validaciones completas

### ✅ Fase 5 - Gestión de Propiedades (CRUD)
- [x] Crear propiedad (POST /api/propiedades) - Solo anfitriones
- [x] Listar mis propiedades (GET /api/propiedades/mis-propiedades)
- [x] Listar propiedades activas (GET /api/propiedades) - Público
- [x] Obtener detalles de propiedad (GET /api/propiedades/{id}) - Público
- [x] Actualizar propiedad (PUT /api/propiedades/{id}) - Solo propietario
- [x] Cambiar estado (PATCH /api/propiedades/{id}/estado)
- [x] Eliminar propiedad (DELETE /api/propiedades/{id}) - Eliminación lógica
- [x] Upload de imágenes (POST /api/files/upload-images)
- [x] DTOs con validaciones completas
- [x] Control de acceso por propietario

### ✅ Fase 6 - Búsqueda y Filtrado Avanzado
- [x] Búsqueda por texto (título y descripción)
- [x] Filtros por ubicación (ciudad, país)
- [x] Filtros por características (habitaciones, camas, baños, capacidad)
- [x] Filtro por rango de precios
- [x] Filtro por tipo de propiedad
- [x] Filtro por servicios (AND lógico)
- [x] Filtro por puntuación mínima
- [x] Filtro por disponibilidad en fechas
- [x] Ordenamiento múltiple (precio, puntuación, reciente, relevancia)
- [x] JPA Specifications para queries dinámicas
- [x] Optimización de rendimiento (<2s)
- [x] Paginación completa

### ✅ Fase 7 - Sistema de Reservas
- [x] Crear reserva (POST /api/reservas) - Solo viajeros
- [x] Confirmar reserva (PUT /api/reservas/{id}/confirmar) - Solo anfitrión
- [x] Rechazar reserva (PUT /api/reservas/{id}/rechazar) - Solo anfitrión
- [x] Cancelar reserva (PUT /api/reservas/{id}/cancelar) - Viajero o anfitrión
- [x] Validación de disponibilidad en fechas
- [x] Bloqueo automático de fechas reservadas
- [x] Validación de estancia mínima/máxima
- [x] Cálculo automático de costos (noche, limpieza, comisión 10%)
- [x] Máquina de estados (PENDIENTE, CONFIRMADA, CANCELADA, etc.)
- [x] Restricción de cancelación (24h antes de check-in)
- [x] Listar reservas por rol (viajero/anfitrión)
- [x] Ver reservas por propiedad

### ✅ Fase 8 - Integración de Pagos (Stripe)
- [x] Integración completa con Stripe SDK v26.13.0
- [x] PaymentIntent API para procesamiento de pagos
- [x] Iniciar pago (POST /api/pagos/iniciar) - Solo viajero
- [x] Webhook de Stripe (POST /api/pagos/webhook) - Verificación de firma
- [x] Consultar pago por reserva (GET /api/pagos/reserva/{id})
- [x] Procesamiento de reembolsos (POST /api/pagos/reembolso/{id})
- [x] Verificación de estado (GET /api/pagos/verificar/{id})
- [x] Conversión automática a centavos (COP)
- [x] Metadata de rastreo (reserva_id, viajero_id)
- [x] Manejo de eventos asíncronos (succeeded/failed)
- [x] Estados de pago (PENDIENTE, COMPLETADO, FALLIDO, REEMBOLSADO)
- [x] Actualización automática de reserva a PAGADA
- [x] Configuración de entorno (test keys)
- [x] Guía completa de testing con tarjetas de prueba

### ✅ Fase 9 - Sistema de Mensajería (WebSockets)
- [x] WebSocket con STOMP protocol para chat en tiempo real
- [x] Autenticación JWT en conexión WebSocket
- [x] SockJS fallback para navegadores sin WebSocket
- [x] Enviar mensaje (POST /api/mensajes)
- [x] Listar conversaciones (GET /api/mensajes/conversaciones)
- [x] Obtener conversación (GET /api/mensajes/conversacion/{id})
- [x] Marcar mensaje como leído (PUT /api/mensajes/{id}/leer)
- [x] Marcar conversación como leída (PUT /api/mensajes/conversacion/{id}/leer)
- [x] Contar mensajes no leídos (GET /api/mensajes/no-leidos/count)
- [x] Mensajes por reserva (GET /api/mensajes/reserva/{id})
- [x] Notificaciones en tiempo real (MENSAJE_NUEVO, MENSAJE_LEIDO)
- [x] Canal privado por usuario (/user/queue/mensajes)
- [x] Asociar mensajes a reservas (opcional)
- [x] Historial persistente de mensajes
- [x] Paginación de conversaciones y mensajes

### ✅ Fase 10 - Sistema de Reputación y Valoraciones
- [x] Crear valoración (POST /api/valoraciones/reserva/{id})
- [x] Responder valoración (PUT /api/valoraciones/{id}/responder)
- [x] Obtener valoraciones de propiedad (GET /api/valoraciones/propiedad/{id})
- [x] Obtener valoraciones de anfitrión (GET /api/valoraciones/anfitrion/{id})
- [x] Obtener mis valoraciones (GET /api/valoraciones/mis-valoraciones)
- [x] Obtener valoración por reserva (GET /api/valoraciones/reserva/{id})
- [x] Estadísticas de propiedad (GET /api/valoraciones/propiedad/{id}/estadisticas)
- [x] Valoraciones pendientes de respuesta (GET /api/valoraciones/pendientes-respuesta)
- [x] Verificar si puede valorar (GET /api/valoraciones/reserva/{id}/puede-valorar)
- [x] Sistema de puntuación 1-5 estrellas
- [x] Comentarios de viajeros (10-2000 caracteres)
- [x] Respuestas de anfitriones (10-1000 caracteres)
- [x] Validación: Solo viajeros valorar reservas COMPLETADAS
- [x] Validación: Solo después de checkout
- [x] Validación: Una valoración por reserva
- [x] Actualización automática de puntuación promedio
- [x] Estadísticas: Promedio, distribución, porcentajes
- [x] Valoraciones públicas (visible=true)
- [x] Control de respuestas duplicadas

### ✅ Fase 11 - Panel de Administración Básico
- [x] Dashboard con métricas generales (GET /api/admin/dashboard)
- [x] Gestión de usuarios: listar, buscar, banear, desbanear, eliminar
- [x] Gestión de propiedades: listar, aprobar, rechazar, toggle visibilidad
- [x] Gestión de reservas: listar, buscar por estado, cancelar (admin override)
- [x] Moderación de valoraciones: ocultar, mostrar
- [x] Métricas completas: usuarios, propiedades, reservas, pagos, valoraciones, mensajes
- [x] KPIs: tasas de conversión, completamiento, cancelación, aprobación
- [x] Filtros avanzados: por rol, estado, fecha
- [x] Validaciones: no banear admins, no eliminar con reservas activas
- [x] Tracking de acciones: baneadoPor, aprobadaPor, rechazadaPor
- [x] Seguridad: todos endpoints con @PreAuthorize("hasRole('ADMIN')")
- [x] 20+ endpoints REST para administración completa

### ✅ Fase 12 - Panel Avanzado (Reportes y Análisis)
- [x] Reporte de ingresos por período (GET /api/reportes/ingresos)
- [x] Reporte de ocupación por propiedad (GET /api/reportes/ocupacion)
- [x] Reporte de comisiones por período (GET /api/reportes/comisiones)
- [x] Top propiedades por ingresos (GET /api/reportes/top-propiedades)
- [x] Top anfitriones por desempeño (GET /api/reportes/top-anfitriones)
- [x] Análisis de estacionalidad por mes (GET /api/reportes/estacionalidad)
- [x] Exportación a PDF (GET /api/reportes/exportar/ingresos/pdf)
- [x] Exportación a Excel (GET /api/reportes/exportar/ingresos/excel)
- [x] Datos para gráficos: ingresos, ocupación, estacionalidad
- [x] Queries nativas optimizadas con PostgreSQL
- [x] Análisis de crecimiento y tasas de cambio
- [x] Métricas avanzadas: ingreso por reserva, tasa de ocupación global
- [x] Períodos flexibles: diario, semanal, mensual, trimestral, anual
- [x] Apache POI para Excel, iText7 para PDF
- [x] 11 endpoints REST con seguridad ADMIN

### ✅ Fase 13 - Sistema de Notificaciones
- [x] Notificaciones in-app (GET /api/notificaciones)
- [x] Notificaciones por email (integración con EmailService)
- [x] Notificaciones en tiempo real (WebSocket)
- [x] 32 tipos de notificaciones (auth, reservas, pagos, mensajes, valoraciones, admin, sistema)
- [x] Marcar como leída (PUT /api/notificaciones/{id}/leer)
- [x] Marcar todas como leídas (PUT /api/notificaciones/marcar-todas-leidas)
- [x] Contar no leídas (GET /api/notificaciones/no-leidas/count)
- [x] Filtrar por tipo (GET /api/notificaciones/tipo/{tipo})
- [x] Estadísticas (GET /api/notificaciones/estadisticas)
- [x] Eliminar notificación (DELETE /api/notificaciones/{id})
- [x] Paginación y ordenamiento
- [x] Templates HTML profesionales para emails
- [x] Integración con eventos de auth, reservas, pagos
- [x] Limpieza automática de notificaciones antiguas
- [x] 7 endpoints REST seguros

### ✅ Fase 14 - Optimización y Performance
- [x] Sistema de caché con Caffeine (6 regiones)
- [x] Cache TTL configurado por tipo (5-30 minutos)
- [x] Anotaciones @Cacheable en servicios (Propiedad, Reserva, Valoración)
- [x] Anotaciones @CacheEvict en actualizaciones
- [x] Pool de conexiones HikariCP optimizado (5-20 conexiones)
- [x] JPA Batch Processing (batch_size=20)
- [x] 20+ índices de base de datos (simples, compuestos, parciales)
- [x] Índices GIN para búsquedas de texto (futuro)
- [x] Compresión HTTP habilitada (JSON, HTML, CSS)
- [x] Performance monitoring con AOP
- [x] Logging de queries lentas (>500ms repositorios, >1s servicios)
- [x] Estadísticas de caché con Actuator
- [x] Configuración optimizada de Hibernate
- [x] Guía de mejores prácticas documentada

### ⏳ Próximas Fases
- [ ] RF11: Soporte y centro de ayuda
- [ ] RF12: Sistema de disputas
- [ ] RF13: Cumplimiento normativo (GDPR)

## Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura
mvn test jacoco:report
```

## Perfiles de Ejecución

### Desarrollo
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Producción
```bash
java -jar target/staykonnect-backend-1.0.0.jar --spring.profiles.active=prod
```

## Endpoints Principales

### Autenticación (✅ Implementado - RF01)
- `POST /api/auth/register` - Registro de usuarios (viajero/anfitrión/admin)
- `POST /api/auth/login` - Login con JWT
- `GET /api/auth/verify-email?token={token}` - Verificar email
- `POST /api/auth/recover-password` - Solicitar recuperación de contraseña
- `POST /api/auth/reset-password` - Resetear contraseña con token

### Propiedades (✅ Implementado - RF02)
- `POST /api/propiedades` - Crear propiedad (solo anfitriones)
- `GET /api/propiedades` - Listar propiedades activas (público)
- `GET /api/propiedades/{id}` - Ver detalles de propiedad (público)
- `GET /api/propiedades/mis-propiedades` - Mis propiedades (solo anfitriones)
- `PUT /api/propiedades/{id}` - Actualizar propiedad (solo propietario o admin)
- `PATCH /api/propiedades/{id}/estado` - Cambiar estado (solo propietario o admin)
- `DELETE /api/propiedades/{id}` - Eliminar propiedad (solo propietario o admin)

### Archivos (✅ Implementado)
- `POST /api/files/upload-image` - Subir imagen
- `POST /api/files/upload-images` - Subir múltiples imágenes
- `POST /api/files/upload-document` - Subir documento
- `DELETE /api/propiedades/{id}` - Eliminar propiedad

### Reservas
- `POST /api/reservas` - Crear reserva
- `GET /api/reservas` - Listar reservas
- `PUT /api/reservas/{id}/confirmar` - Confirmar reserva

*(Documentación completa en Swagger)*

## Variables de Entorno (Producción)

```bash
DATABASE_URL=jdbc:postgresql://host:5432/dbname
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
JWT_SECRET=your-secret-key-256-bits
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email
MAIL_PASSWORD=your-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

## Contribución

1. Fork el proyecto
2. Crear una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto es parte del caso de estudio StayKonnect.

## Contacto

Equipo de Desarrollo StayKonnect
