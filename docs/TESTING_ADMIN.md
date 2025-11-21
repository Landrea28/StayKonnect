# Guía de Testing - Panel de Administración

Esta guía describe cómo probar el sistema de administración del panel de StayKonnect.

## Prerrequisitos

1. **Backend corriendo** en `http://localhost:8080`
2. **Usuario administrador** creado en la base de datos
3. **Datos de prueba** (usuarios, propiedades, reservas)

## Crear Usuario Administrador

```sql
-- Conectar a PostgreSQL y ejecutar:
UPDATE usuario 
SET rol = 'ADMIN', activo = true, email_verificado = true 
WHERE email = 'admin@staykonnect.com';
```

## Autenticación

```bash
# Login como administrador
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@staykonnect.com","password":"Admin1234@"}' | jq -r '.data.token'

export TOKEN_ADMIN="tu_token_aqui"
```

## 1. Dashboard - Métricas Generales

### Obtener Métricas del Dashboard

```bash
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Métricas obtenidas exitosamente",
  "data": {
    "totalUsuarios": 1250,
    "usuariosActivos": 980,
    "usuariosBaneados": 15,
    "nuevoUsuariosUltimos30Dias": 127,
    "usuariosPorRol": {
      "VIAJERO": 950,
      "ANFITRION": 280,
      "ADMIN": 20
    },
    "totalPropiedades": 450,
    "propiedadesAprobadas": 380,
    "propiedadesPendientes": 50,
    "propiedadesRechazadas": 20,
    "propiedadesPorTipo": {
      "CASA": 150,
      "APARTAMENTO": 200,
      "HABITACION": 100
    },
    "totalReservas": 3500,
    "reservasPendientes": 45,
    "reservasConfirmadas": 180,
    "reservasCompletadas": 2890,
    "reservasCanceladas": 385,
    "reservasPorEstado": {
      "PENDIENTE": 45,
      "CONFIRMADA": 180,
      "COMPLETADA": 2890,
      "CANCELADA": 385
    },
    "ingresosTotales": 875000.00,
    "ingresosMesActual": 125000.00,
    "comisionesTotales": 87500.00,
    "comisionesMesActual": 12500.00,
    "pagosPendientes": 25,
    "pagosCompletados": 3100,
    "pagosFallidos": 87,
    "totalValoraciones": 2500,
    "puntuacionPromedioGeneral": 4.6,
    "valoracionesPendientesModeracion": 0,
    "valoracionesOcultas": 12,
    "totalMensajes": 18500,
    "mensajesUltimos30Dias": 3200,
    "conversacionesActivas": 450,
    "tasaConversionReservas": 89.5,
    "tasaCompletamiento": 94.2,
    "tasaCancelacion": 11.0,
    "tasaAprobacionPropiedades": 84.4
  }
}
```

**Uso en UI:**
```javascript
// Dashboard de administración con cards
- Card "Usuarios": totalUsuarios, nuevoUsuariosUltimos30Dias
- Card "Propiedades": propiedadesPendientes (acción requerida)
- Card "Reservas": reservasPendientes (acción requerida)
- Card "Ingresos": ingresosMesActual, comisionesMesActual
- Gráficos de torta: usuariosPorRol, propiedadesPorTipo, reservasPorEstado
- KPIs: tasaConversionReservas, tasaCompletamiento, tasaCancelacion
```

## 2. Gestión de Usuarios

### Listar Todos los Usuarios

```bash
curl -X GET "http://localhost:8080/api/admin/usuarios?page=0&size=20&ordenarPor=createdDate&direccion=DESC" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Buscar Usuarios

```bash
# Por nombre/email
curl -X GET "http://localhost:8080/api/admin/usuarios/buscar?query=juan" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# Por rol
curl -X GET "http://localhost:8080/api/admin/usuarios/buscar?rol=ANFITRION" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# Usuarios baneados
curl -X GET "http://localhost:8080/api/admin/usuarios/buscar?baneado=true" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Banear Usuario

```bash
curl -X PUT http://localhost:8080/api/admin/usuarios/5/banear \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "razon": "Comportamiento inapropiado reportado múltiples veces por otros usuarios. Violación de términos de servicio."
  }' | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario baneado exitosamente",
  "data": {
    "id": 5,
    "nombre": "Juan",
    "apellido": "Problemático",
    "email": "juan@test.com",
    "rol": "VIAJERO",
    "activo": false,
    "baneado": true,
    "fechaBaneo": "2024-11-19T16:30:00",
    "razonBaneo": "Comportamiento inapropiado reportado múltiples veces...",
    "baneadoPorNombre": "Admin Principal"
  }
}
```

### Desbanear Usuario

```bash
curl -X PUT http://localhost:8080/api/admin/usuarios/5/desbanear \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Eliminar Usuario

```bash
curl -X DELETE http://localhost:8080/api/admin/usuarios/5 \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Validaciones:**
- ❌ No se puede eliminar usuarios con reservas activas
- ❌ No se puede eliminar administradores
- ✅ Se elimina permanentemente si cumple requisitos

## 3. Gestión de Propiedades

### Listar Todas las Propiedades

```bash
curl -X GET "http://localhost:8080/api/admin/propiedades?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Listar Propiedades Pendientes de Aprobación

```bash
curl -X GET "http://localhost:8080/api/admin/propiedades/pendientes?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Propiedades pendientes obtenidas",
  "data": {
    "content": [
      {
        "id": 125,
        "titulo": "Casa moderna en el centro",
        "descripcion": "Hermosa casa con 3 habitaciones...",
        "tipo": "CASA",
        "estado": "PENDIENTE_APROBACION",
        "precioPorNoche": 150.00,
        "ciudad": "Bogotá",
        "pais": "Colombia",
        "capacidadMaxima": 6,
        "aprobada": false,
        "rechazada": false,
        "createdDate": "2024-11-19T10:00:00",
        "anfitrionId": 45,
        "anfitrionNombre": "María García",
        "anfitrionEmail": "maria@test.com",
        "totalReservas": 0,
        "totalValoraciones": 0,
        "puntuacionPromedio": 0.0
      }
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

### Aprobar Propiedad

```bash
curl -X PUT http://localhost:8080/api/admin/propiedades/125/aprobar \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Efectos:**
- ✅ aprobada = true
- ✅ fechaAprobacion = now()
- ✅ aprobadaPor = admin
- ✅ Limpia rechazo anterior (si existía)
- 📧 TODO: Enviar email al anfitrión

### Rechazar Propiedad

```bash
curl -X PUT http://localhost:8080/api/admin/propiedades/126/rechazar \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "razon": "Las imágenes no muestran claramente el interior de la propiedad. Por favor, agregue fotos de mejor calidad de cada habitación, baño y áreas comunes."
  }' | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Propiedad rechazada",
  "data": {
    "id": 126,
    "titulo": "Apartamento sin fotos claras",
    "rechazada": true,
    "fechaRechazo": "2024-11-19T16:45:00",
    "razonRechazo": "Las imágenes no muestran claramente...",
    "rechazadaPorNombre": "Admin Principal"
  }
}
```

### Cambiar Visibilidad de Propiedad

```bash
curl -X PUT http://localhost:8080/api/admin/propiedades/125/toggle-visibilidad \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Uso:** Ocultar temporalmente propiedades problemáticas sin rechazarlas

## 4. Gestión de Reservas

### Listar Todas las Reservas

```bash
curl -X GET "http://localhost:8080/api/admin/reservas?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Buscar Reservas por Estado

```bash
# Reservas pendientes
curl -X GET "http://localhost:8080/api/admin/reservas/estado/PENDIENTE?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# Reservas confirmadas
curl -X GET "http://localhost:8080/api/admin/reservas/estado/CONFIRMADA" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Reservas filtradas obtenidas",
  "data": {
    "content": [
      {
        "id": 450,
        "estado": "PENDIENTE",
        "fechaCheckin": "2024-12-01T14:00:00",
        "fechaCheckout": "2024-12-05T12:00:00",
        "numeroHuespedes": 4,
        "precioTotal": 720.00,
        "viajeroId": 89,
        "viajeroNombre": "Carlos López",
        "viajeroEmail": "carlos@test.com",
        "viajeroTelefono": "+573001234567",
        "anfitrionId": 45,
        "anfitrionNombre": "María García",
        "anfitrionEmail": "maria@test.com",
        "propiedadId": 125,
        "propiedadTitulo": "Casa moderna en el centro",
        "propiedadCiudad": "Bogotá",
        "propiedadPais": "Colombia",
        "pagoEstado": "PENDIENTE",
        "pagoMetodo": "TARJETA_CREDITO",
        "tieneValoracion": false
      }
    ]
  }
}
```

### Cancelar Reserva (Admin Override)

```bash
curl -X PUT "http://localhost:8080/api/admin/reservas/450/cancelar?razon=Propiedad+reportada+con+problemas+estructurales" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Uso:**
- Cancelar reservas por problemas con la propiedad
- Cancelar por disputas irresolubles
- Cancelar por situaciones de emergencia

**Efectos:**
- ✅ estado = CANCELADA
- 📧 TODO: Notificar a viajero y anfitrión
- 💰 TODO: Procesar reembolso automático

## 5. Moderación de Valoraciones

### Ocultar Valoración Inapropiada

```bash
curl -X PUT http://localhost:8080/api/admin/valoraciones/123/ocultar \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

**Uso:**
- Valoraciones con lenguaje ofensivo
- Valoraciones fraudulentas
- Valoraciones spam o irrelevantes

### Mostrar Valoración Previamente Oculta

```bash
curl -X PUT http://localhost:8080/api/admin/valoraciones/123/mostrar \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

## Casos de Prueba

### Escenario 1: Revisión de Propiedades Pendientes

```bash
# 1. Ver dashboard - identificar propiedades pendientes
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq '.data.propiedadesPendientes'

# 2. Listar propiedades pendientes
curl -X GET http://localhost:8080/api/admin/propiedades/pendientes \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# 3. Aprobar propiedad válida
curl -X PUT http://localhost:8080/api/admin/propiedades/125/aprobar \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# 4. Rechazar propiedad con problemas
curl -X PUT http://localhost:8080/api/admin/propiedades/126/rechazar \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"razon": "Información incompleta"}'
```

### Escenario 2: Gestión de Usuario Problemático

```bash
# 1. Buscar usuario reportado
curl -X GET "http://localhost:8080/api/admin/usuarios/buscar?query=juan@test.com" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# 2. Ver detalles del usuario
# (incluye total de reservas, valoraciones, etc.)

# 3. Banear usuario
curl -X PUT http://localhost:8080/api/admin/usuarios/5/banear \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"razon": "Múltiples reportes de comportamiento inapropiado"}'

# 4. Verificar baneo
curl -X GET "http://localhost:8080/api/admin/usuarios/buscar?baneado=true" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq
```

### Escenario 3: Cancelación de Reserva por Emergencia

```bash
# 1. Buscar reserva
curl -X GET "http://localhost:8080/api/admin/reservas/estado/CONFIRMADA" \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq

# 2. Cancelar reserva con razón
curl -X PUT "http://localhost:8080/api/admin/reservas/450/cancelar?razon=Propiedad+afectada+por+inundacion" \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# 3. Verificar cancelación
curl -X GET http://localhost:8080/api/admin/reservas/450 \
  -H "Authorization: Bearer $TOKEN_ADMIN" | jq '.data.estado'
```

## Validaciones de Seguridad

### ❌ Usuario NO Admin Intenta Acceder

```bash
# Login como viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"viajero@test.com","password":"Pass1234@"}' | jq -r '.data.token'

export TOKEN_VIAJERO="token_viajero"

# Intentar acceder al dashboard
curl -X GET http://localhost:8080/api/admin/dashboard \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq

# Error esperado: 403 Forbidden
{
  "success": false,
  "message": "Acceso denegado. Se requiere rol ADMIN"
}
```

### ❌ Admin No Puede Banear a Otro Admin

```bash
curl -X PUT http://localhost:8080/api/admin/usuarios/1/banear \
  -H "Authorization: Bearer $TOKEN_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"razon": "Test"}'

# Error esperado: 400 Bad Request
{
  "success": false,
  "message": "No se puede banear a un administrador"
}
```

### ❌ No Se Puede Eliminar Usuario con Reservas Activas

```bash
curl -X DELETE http://localhost:8080/api/admin/usuarios/45 \
  -H "Authorization: Bearer $TOKEN_ADMIN"

# Error esperado: 400 Bad Request
{
  "success": false,
  "message": "No se puede eliminar un usuario con reservas activas. Tiene 3 reserva(s) pendiente(s). Considere banearlo en su lugar."
}
```

## UI/UX Recomendado

### Dashboard de Administración

```
┌────────────────────────────────────────────────┐
│  DASHBOARD DE ADMINISTRACIÓN                   │
├────────────────────────────────────────────────┤
│                                                 │
│  📊 KPIs Principales                           │
│  ┌──────────┬──────────┬──────────┬──────────┐│
│  │ Usuarios │Propiedades│ Reservas │ Ingresos ││
│  │  1,250   │   450    │  3,500   │ $875K    ││
│  │  +127    │   +50    │   +180   │ +$125K   ││
│  │ (30 días)│(Pendientes│(Activas) │(Este mes)││
│  └──────────┴──────────┴──────────┴──────────┘│
│                                                 │
│  ⚠️  Acciones Requeridas                       │
│  • 50 propiedades pendientes de aprobación     │
│  • 45 reservas pendientes de confirmación      │
│  • 15 usuarios reportados                      │
│                                                 │
│  📈 Métricas de Conversión                     │
│  • Tasa de conversión: 89.5%                   │
│  • Tasa de completamiento: 94.2%               │
│  • Tasa de cancelación: 11.0%                  │
│                                                 │
│  📊 Gráficos (torta y barras)                  │
│  [Usuarios por Rol] [Propiedades por Tipo]     │
│  [Reservas por Estado]                         │
└────────────────────────────────────────────────┘
```

### Panel de Gestión de Usuarios

```
┌────────────────────────────────────────────────┐
│  GESTIÓN DE USUARIOS                           │
├────────────────────────────────────────────────┤
│  🔍 [Buscar...] [Rol ▼] [Estado ▼] [Filtrar]  │
├────────────────────────────────────────────────┤
│  ID  │ Nombre      │ Email       │ Rol  │Estado│
│──────┼─────────────┼─────────────┼──────┼──────│
│  45  │ María G.    │ maria@...   │ANFIT.│ ✅   │
│      │ [Ver] [Banear] [Eliminar]              │
│──────┼─────────────┼─────────────┼──────┼──────│
│  89  │ Carlos L.   │ carlos@...  │VIAJ. │ ✅   │
│      │ [Ver] [Banear] [Eliminar]              │
│──────┼─────────────┼─────────────┼──────┼──────│
│   5  │ Juan P.     │ juan@...    │VIAJ. │ 🚫   │
│      │ BANEADO: Comportamiento inapropiado    │
│      │ [Ver] [Desbanear]                      │
└────────────────────────────────────────────────┘
```

## Testing con Postman

1. **Importar colección** con 20+ endpoints de admin
2. **Configurar variables:**
   - `base_url`: http://localhost:8080
   - `token_admin`: JWT del administrador
3. **Ejecutar en orden:**
   - Dashboard → Usuarios → Propiedades → Reservas → Moderación

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Administración"**
2. Autoriza con JWT de admin
3. Prueba endpoints en orden lógico

## Métricas de Rendimiento

- **Dashboard:** < 500ms (con caché recomendado)
- **Listados:** < 200ms por página
- **Operaciones CRUD:** < 100ms

## Próximos Pasos

Una vez validado el panel básico:

- **Paso 12:** Panel avanzado (reportes PDF/Excel, gráficos, análisis profundo)
- **Paso 13:** Sistema de notificaciones (email al banear, aprobar, etc.)
- **Paso 14:** Logs de auditoría (tracking de acciones de admin)

## Recursos Adicionales

- [Admin Panel Best Practices](https://uxplanet.org/designing-admin-panels-5-common-mistakes-to-avoid-9c9c3f5e3f3f)
- [Role-Based Access Control](https://auth0.com/docs/manage-users/access-control/rbac)
