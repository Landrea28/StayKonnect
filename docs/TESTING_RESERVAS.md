# Guía de Testing - Sistema de Reservas

Esta guía describe cómo probar el sistema completo de gestión de reservas.

## Prerrequisitos

1. Backend corriendo en `http://localhost:8080`
2. Base de datos con propiedades activas
3. Usuarios de prueba:
   - **Viajero**: `maria.viajera@test.com` / `Test1234@`
   - **Anfitrión**: `carlos.anfitrion@test.com` / `Test1234@`

## Flujo Completo de Reserva

### Paso 1: Login como Viajero

```bash
# Obtener token de viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria.viajera@test.com",
    "password": "Test1234@"
  }' | jq

# Guardar el token
export TOKEN_VIAJERO="tu_token_aqui"
```

### Paso 2: Buscar Propiedad Disponible

```bash
# Buscar propiedades disponibles en fechas específicas
curl -X GET "http://localhost:8080/api/propiedades/buscar?\
fechaInicio=2024-12-01&\
fechaFin=2024-12-10&\
ciudad=Cartagena" | jq

# Anotar el propiedadId para usar en la reserva
```

### Paso 3: Crear Reserva (Viajero)

```bash
# Crear solicitud de reserva
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId": 1,
    "fechaCheckin": "2024-12-01",
    "fechaCheckout": "2024-12-10",
    "numeroHuespedes": 4,
    "notasEspeciales": "Llegada aproximadamente a las 15:00"
  }' | jq

# Respuesta esperada: Estado PENDIENTE
```

### Paso 4: Login como Anfitrión

```bash
# Obtener token de anfitrión
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.anfitrion@test.com",
    "password": "Test1234@"
  }' | jq

export TOKEN_ANFITRION="tu_token_aqui"
```

### Paso 5: Ver Reservas Recibidas (Anfitrión)

```bash
# Listar reservas pendientes
curl -X GET http://localhost:8080/api/reservas/recibidas \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq

# Anotar el reservaId
```

### Paso 6: Confirmar Reserva (Anfitrión)

```bash
# Confirmar la reserva
curl -X PUT http://localhost:8080/api/reservas/1/confirmar \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq

# Respuesta esperada: Estado CONFIRMADA
```

## Endpoints Disponibles

### 1. Crear Reserva (POST /api/reservas)

**Permisos:** Solo VIAJERO

```bash
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId": 1,
    "fechaCheckin": "2024-12-15",
    "fechaCheckout": "2024-12-20",
    "numeroHuespedes": 2,
    "notasEspeciales": "¿Es posible early check-in?"
  }' | jq
```

**Validaciones:**
- ✅ Propiedad existe y está ACTIVA
- ✅ Viajero no es el anfitrión de la propiedad
- ✅ Checkout posterior a checkin
- ✅ Respeta estancia mínima/máxima
- ✅ No hay reservas conflictivas (CONFIRMADA, PAGADA, EN_CURSO)
- ✅ Capacidad suficiente
- ✅ Reserva máximo 1 año anticipación

**Cálculo de costos:**
- Subtotal = precioPorNoche × numeroNoches
- Comisión plataforma = 10% del subtotal
- Total = subtotal + precioLimpieza + comisión

### 2. Confirmar Reserva (PUT /api/reservas/{id}/confirmar)

**Permisos:** Solo ANFITRION (propietario)

```bash
curl -X PUT http://localhost:8080/api/reservas/1/confirmar \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

**Validaciones:**
- ✅ Reserva existe
- ✅ Usuario es el anfitrión de la propiedad
- ✅ Estado actual es PENDIENTE
- ✅ Sigue disponible (doble verificación)

### 3. Rechazar Reserva (PUT /api/reservas/{id}/rechazar)

**Permisos:** Solo ANFITRION (propietario)

```bash
curl -X PUT "http://localhost:8080/api/reservas/1/rechazar?motivo=No%20disponible%20en%20esas%20fechas" \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

**Validaciones:**
- ✅ Solo reservas PENDIENTE
- ✅ Motivo obligatorio
- ✅ Estado final: CANCELADA

### 4. Cancelar Reserva (PUT /api/reservas/{id}/cancelar)

**Permisos:** VIAJERO (propio) o ANFITRION (de su propiedad)

```bash
curl -X PUT http://localhost:8080/api/reservas/1/cancelar \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "motivoCancelacion": "Cambio de planes por motivos laborales"
  }' | jq
```

**Validaciones:**
- ✅ Usuario es viajero o anfitrión de la reserva
- ✅ Estado PENDIENTE o CONFIRMADA
- ✅ Check-in en más de 24 horas
- ✅ Motivo 10-500 caracteres

### 5. Obtener Detalles de Reserva (GET /api/reservas/{id})

**Permisos:** VIAJERO (propio), ANFITRION (de su propiedad), ADMIN

```bash
curl -X GET http://localhost:8080/api/reservas/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

**Respuesta incluye:**
- Datos completos de la reserva
- Información de la propiedad
- Datos del viajero y anfitrión
- Desglose de costos

### 6. Listar Mis Reservas como Viajero (GET /api/reservas/mis-reservas)

**Permisos:** Solo VIAJERO

```bash
curl -X GET "http://localhost:8080/api/reservas/mis-reservas?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

### 7. Listar Reservas Recibidas (GET /api/reservas/recibidas)

**Permisos:** Solo ANFITRION

```bash
curl -X GET "http://localhost:8080/api/reservas/recibidas?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

### 8. Listar Reservas por Propiedad (GET /api/reservas/propiedad/{propiedadId})

**Permisos:** ANFITRION (propietario) o ADMIN

```bash
curl -X GET "http://localhost:8080/api/reservas/propiedad/1?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

## Casos de Prueba por Escenario

### Escenario 1: Reserva Exitosa Completa

```bash
# 1. Viajero crea reserva
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-01","fechaCheckout":"2024-12-05","numeroHuespedes":2}'

# 2. Anfitrión confirma
curl -X PUT http://localhost:8080/api/reservas/1/confirmar \
  -H "Authorization: Bearer $TOKEN_ANFITRION"

# ✅ Estado final: CONFIRMADA
```

### Escenario 2: Anfitrión Rechaza Reserva

```bash
# 1. Viajero crea reserva
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-10","fechaCheckout":"2024-12-15","numeroHuespedes":4}'

# 2. Anfitrión rechaza
curl -X PUT "http://localhost:8080/api/reservas/2/rechazar?motivo=Mantenimiento%20programado" \
  -H "Authorization: Bearer $TOKEN_ANFITRION"

# ✅ Estado final: CANCELADA (por rechazo)
```

### Escenario 3: Viajero Cancela Reserva

```bash
# 1. Crear y confirmar reserva (pasos anteriores)

# 2. Viajero cancela
curl -X PUT http://localhost:8080/api/reservas/1/cancelar \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"motivoCancelacion":"Emergencia familiar, no podré viajar"}'

# ✅ Estado final: CANCELADA
```

### Escenario 4: Fechas No Disponibles

```bash
# Crear primera reserva confirmada
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-01","fechaCheckout":"2024-12-10","numeroHuespedes":2}'

curl -X PUT http://localhost:8080/api/reservas/1/confirmar \
  -H "Authorization: Bearer $TOKEN_ANFITRION"

# Intentar reservar fechas que se solapan
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-05","fechaCheckout":"2024-12-12","numeroHuespedes":2}'

# ❌ Error: "La propiedad no está disponible en las fechas seleccionadas"
```

### Escenario 5: Violación de Estancia Mínima

```bash
# Propiedad con estanciaMinima = 3 noches
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-01","fechaCheckout":"2024-12-02","numeroHuespedes":2}'

# ❌ Error: "La estancia mínima es de 3 noches"
```

### Escenario 6: Exceso de Capacidad

```bash
# Propiedad con capacidad = 4
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"propiedadId":1,"fechaCheckin":"2024-12-01","fechaCheckout":"2024-12-05","numeroHuespedes":6}'

# ❌ Error: "La propiedad tiene capacidad máxima de 4 huéspedes"
```

### Escenario 7: Cancelación Tardía (< 24h)

```bash
# Reserva con check-in mañana
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId":1,
    "fechaCheckin":"2024-11-20",
    "fechaCheckout":"2024-11-25",
    "numeroHuespedes":2
  }'

# Confirmar
curl -X PUT http://localhost:8080/api/reservas/1/confirmar \
  -H "Authorization: Bearer $TOKEN_ANFITRION"

# Intentar cancelar (menos de 24h antes)
curl -X PUT http://localhost:8080/api/reservas/1/cancelar \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"motivoCancelacion":"Cambio de planes"}'

# ❌ Error: "No se puede cancelar una reserva con menos de 24 horas antes del check-in"
```

## Validaciones de Negocio

### Estados Válidos de Reserva

```
PENDIENTE → CONFIRMADA → PAGADA → EN_CURSO → COMPLETADA
    ↓           ↓          ↓
CANCELADA   CANCELADA  CANCELADA
```

### Reglas de Disponibilidad

Una propiedad está **NO DISPONIBLE** si existe una reserva con:
- Estado: CONFIRMADA, PAGADA, o EN_CURSO
- Fechas que se solapan de alguna forma:
  - Solapamiento inicio
  - Solapamiento fin
  - Contenida completamente
  - Contiene completamente

### Permisos por Rol

| Acción | VIAJERO | ANFITRION | ADMIN |
|--------|---------|-----------|-------|
| Crear reserva | ✅ | ❌ | ❌ |
| Ver propia reserva | ✅ | ✅ | ✅ |
| Confirmar reserva | ❌ | ✅ (propia) | ❌ |
| Rechazar reserva | ❌ | ✅ (propia) | ❌ |
| Cancelar reserva | ✅ (propia) | ✅ (propia) | ✅ |
| Ver todas las reservas | ❌ | ❌ | ✅ |

## Estructura de Respuesta

### ReservaDTO (completa)

```json
{
  "success": true,
  "message": "Reserva creada exitosamente",
  "data": {
    "id": 1,
    "estado": "PENDIENTE",
    "fechaCheckin": "2024-12-01",
    "fechaCheckout": "2024-12-10",
    "numeroNoches": 9,
    "numeroHuespedes": 4,
    "notasEspeciales": "Llegada aproximada 15:00",
    "precioTotal": 3465000,
    "precioNoche": 350000,
    "precioLimpieza": 50000,
    "depositoSeguridad": 200000,
    "comisionPlataforma": 315000,
    "propiedadId": 1,
    "propiedadTitulo": "Villa de Lujo Frente al Mar",
    "propiedadDireccion": "Calle 123 #45-67",
    "propiedadCiudad": "Cartagena",
    "propiedadImagenPrincipal": "http://localhost:8080/images/villa-uuid.jpg",
    "viajeroId": 2,
    "viajeroNombre": "Maria Lopez",
    "viajeroEmail": "maria.viajera@test.com",
    "viajeroTelefono": "+57 300 1234567",
    "anfitrionId": 3,
    "anfitrionNombre": "Carlos Mendoza",
    "anfitrionEmail": "carlos.anfitrion@test.com",
    "anfitrionTelefono": "+57 310 7654321",
    "createdDate": "2024-11-19T10:30:00",
    "lastModifiedDate": "2024-11-19T10:30:00",
    "fechaConfirmacion": null,
    "fechaCancelacion": null
  }
}
```

### ReservaResumenDTO (listados)

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "estado": "CONFIRMADA",
        "fechaCheckin": "2024-12-01",
        "fechaCheckout": "2024-12-10",
        "numeroNoches": 9,
        "numeroHuespedes": 4,
        "precioTotal": 3465000,
        "propiedadId": 1,
        "propiedadTitulo": "Villa de Lujo Frente al Mar",
        "propiedadCiudad": "Cartagena",
        "propiedadImagenPrincipal": "http://localhost:8080/images/villa.jpg",
        "contraparteNombre": "Carlos Mendoza",
        "contraparteEmail": "carlos.anfitrion@test.com"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

## Errores Comunes

### 400 Bad Request - Fechas Inválidas

```json
{
  "success": false,
  "message": "La fecha de check-out debe ser posterior a la de check-in"
}
```

### 403 Forbidden - Sin Permisos

```json
{
  "success": false,
  "message": "Solo los usuarios con rol VIAJERO pueden hacer reservas"
}
```

### 404 Not Found - Reserva No Existe

```json
{
  "success": false,
  "message": "Reserva no encontrada"
}
```

### 400 Bad Request - No Disponible

```json
{
  "success": false,
  "message": "La propiedad no está disponible en las fechas seleccionadas"
}
```

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Reservas"** en el listado
2. Haz clic en **"Authorize"** e ingresa tu token JWT
3. Prueba los endpoints directamente desde la interfaz

## Próximos Pasos

Una vez validado el sistema de reservas:

- **Paso 8:** Integrar pasarela de pago (Stripe/PayPal)
- **Paso 9:** Actualizar estado a PAGADA tras confirmación
- **Paso 10:** Implementar sistema de mensajería entre viajero y anfitrión
