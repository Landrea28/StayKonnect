# Resumen - Paso 10: Sistema de Reputación y Valoraciones

**Fecha:** 19 de noviembre, 2024  
**Fase:** RF07 - Sistema de Reputación  
**Progreso:** 10/20 (50% del proyecto)

---

## 🎯 Objetivo Completado

Implementar sistema completo de valoraciones y reputación que permite a los viajeros calificar propiedades después de reservas completadas, con respuestas de anfitriones y estadísticas públicas.

---

## 📊 Estadísticas de Implementación

| Métrica | Valor |
|---------|-------|
| Archivos creados | 7 |
| Archivos modificados | 3 |
| Líneas de código | ~1,200 |
| Endpoints REST | 9 |
| Validaciones implementadas | 12 |
| Consultas SQL custom | 10 |

---

## 🗃️ Estructura de Base de Datos

### Nueva Tabla: `valoracion`

```sql
CREATE TABLE valoracion (
    id BIGSERIAL PRIMARY KEY,
    puntuacion INTEGER NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
    comentario TEXT,
    respuesta_anfitrion TEXT,
    fecha_respuesta TIMESTAMP,
    visible BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP,
    reserva_id BIGINT UNIQUE REFERENCES reserva(id),
    valorador_id BIGINT REFERENCES usuario(id),
    valorado_id BIGINT REFERENCES usuario(id),
    propiedad_id BIGINT REFERENCES propiedad(id)
);
```

**Índices creados (9):**
- `idx_valoracion_reserva_id` (UNIQUE)
- `idx_valoracion_valorador_id`
- `idx_valoracion_valorado_id`
- `idx_valoracion_propiedad_id`
- `idx_valoracion_puntuacion`
- `idx_valoracion_visible`
- `idx_valoracion_created_date`
- `idx_valoracion_propiedad_visible` (composite)
- `idx_valoracion_valorado_visible` (composite)

---

## 🏗️ Arquitectura Implementada

### 1. Capa de Entidad

**`Valoracion.java`** (Entity)
- Relaciones: OneToOne con Reserva, ManyToOne con Usuario (valorador, valorado), ManyToOne con Propiedad
- Campos: puntuacion (1-5), comentario, respuestaAnfitrion, fechaRespuesta, visible
- Auditoría: createdDate, lastModifiedDate con EntityListeners
- Métodos útiles:
  - `responder(String)`: Guarda respuesta del anfitrión
  - `tieneRespuesta()`: Verifica si tiene respuesta
  - `puedeSerRespondidaPor(Long)`: Valida permisos de respuesta

### 2. Capa de Repositorio

**`ValoracionRepository.java`** (10 queries custom)

**Consultas de valoraciones:**
- `findByPropiedadIdAndVisibleTrue()`: Valoraciones de propiedad (paginadas)
- `findByValoradoIdAndVisibleTrue()`: Valoraciones de anfitrión (paginadas)
- `findByValoradorId()`: Valoraciones del usuario (paginadas)

**Estadísticas:**
- `calcularPromedioPropiedad()`: Promedio de puntuaciones
- `calcularPromedioAnfitrion()`: Promedio del anfitrión
- `contarValoracionesPropiedad()`: Total de valoraciones
- `contarValoracionesAnfitrion()`: Total del anfitrión

**Gestión:**
- `findValoracionesPendientesRespuesta()`: Sin responder (paginadas)
- `obtenerDistribucionPuntuaciones()`: Distribución por estrellas
- `findValoracionesRecientes()`: Últimas 10 valoraciones

### 3. Capa de DTOs

**CrearValoracionRequest.java**
```java
{
  "puntuacion": 5,           // Required, 1-5
  "comentario": "Excelente..." // Optional, 10-2000 chars
}
```

**ResponderValoracionRequest.java**
```java
{
  "respuesta": "Gracias..."  // Required, 10-1000 chars
}
```

**ValoracionDTO.java** (20+ campos)
- Información completa de valoración
- Datos de reserva (fechas)
- Datos de valorador (nombre, foto)
- Datos de valorado (nombre, foto)
- Datos de propiedad (título, imagen)

**EstadisticasValoracionDTO.java**
```java
{
  "promedioGeneral": 4.6,
  "totalValoraciones": 50,
  "distribucionPuntuaciones": {
    "5": 35, "4": 10, "3": 3, "2": 1, "1": 1
  },
  "porcentaje5Estrellas": 70.0,
  "porcentaje4Estrellas": 20.0,
  "porcentaje3Estrellas": 6.0,
  "porcentaje2Estrellas": 2.0,
  "porcentaje1Estrella": 2.0
}
```

### 4. Capa de Servicio

**`ValoracionService.java`** (9 métodos públicos)

**Creación y respuesta:**
- `crearValoracion(Long reservaId, CrearValoracionRequest)`:
  - ✅ Valida que usuario sea el viajero
  - ✅ Valida estado COMPLETADA
  - ✅ Valida que checkout haya pasado
  - ✅ Valida que no exista valoración previa
  - ✅ Actualiza puntuación promedio de propiedad
  
- `responderValoracion(Long valoracionId, ResponderValoracionRequest)`:
  - ✅ Valida que usuario sea el anfitrión (valorado)
  - ✅ Valida que no tenga respuesta previa
  - ✅ Guarda respuesta con timestamp

**Consultas:**
- `obtenerValoracionesPropiedad()`: Paginadas, solo visibles
- `obtenerValoracionesAnfitrion()`: Paginadas, solo visibles
- `obtenerMisValoraciones()`: Del usuario actual
- `obtenerValoracionPorReserva()`: Con validación de permisos

**Estadísticas:**
- `obtenerEstadisticasPropiedad()`: Calcula promedio, distribución, porcentajes

**Gestión:**
- `obtenerValoracionesPendientesRespuesta()`: Para dashboard del anfitrión
- `puedeValorarReserva()`: Validación previa a creación

### 5. Capa de Controlador

**`ValoracionController.java`** (9 endpoints)

| Método | Endpoint | Rol | Descripción |
|--------|----------|-----|-------------|
| POST | `/api/valoraciones/reserva/{id}` | VIAJERO | Crear valoración |
| PUT | `/api/valoraciones/{id}/responder` | ANFITRION | Responder valoración |
| GET | `/api/valoraciones/propiedad/{id}` | PÚBLICO | Valoraciones de propiedad |
| GET | `/api/valoraciones/anfitrion/{id}` | PÚBLICO | Valoraciones de anfitrión |
| GET | `/api/valoraciones/mis-valoraciones` | VIAJERO/ANFITRION | Mis valoraciones |
| GET | `/api/valoraciones/reserva/{id}` | VIAJERO/ANFITRION | Valoración por reserva |
| GET | `/api/valoraciones/propiedad/{id}/estadisticas` | PÚBLICO | Estadísticas |
| GET | `/api/valoraciones/pendientes-respuesta` | ANFITRION | Sin responder |
| GET | `/api/valoraciones/reserva/{id}/puede-valorar` | VIAJERO | Verificar permisos |

---

## 🔐 Configuración de Seguridad

```java
// SecurityConfig.java - Nuevas reglas
http.authorizeHttpRequests(auth -> auth
    // Endpoints públicos (ver valoraciones)
    .requestMatchers(GET, "/api/valoraciones/propiedad/**").permitAll()
    .requestMatchers(GET, "/api/valoraciones/anfitrion/**").permitAll()
    .requestMatchers(GET, "/api/valoraciones/*/estadisticas").permitAll()
    
    // Endpoints autenticados (CRUD)
    .requestMatchers("/api/valoraciones/**")
        .hasAnyRole("VIAJERO", "ANFITRION")
);
```

---

## ✅ Reglas de Negocio Implementadas

### Restricciones de Creación

1. **Solo viajeros pueden valorar:**
   - Validación: `reserva.getViajero().getId().equals(usuarioId)`

2. **Solo reservas completadas:**
   - Validación: `reserva.getEstado() == EstadoReserva.COMPLETADA`

3. **Solo después del checkout:**
   - Validación: `reserva.getFechaCheckout().isBefore(LocalDateTime.now())`

4. **Una valoración por reserva:**
   - Constraint: `UNIQUE` en `reserva_id`
   - Validación: `valoracionRepository.existsByReservaId()`

### Restricciones de Respuesta

1. **Solo anfitrión puede responder:**
   - Validación: `valoracion.puedeSerRespondidaPor(usuarioId)`

2. **Una respuesta por valoración:**
   - Validación: `!valoracion.tieneRespuesta()`

3. **Respuesta permanente:**
   - No se permite editar/borrar respuesta

### Actualización Automática

**Puntuación promedio de propiedad:**
```java
Double promedio = valoracionRepository.calcularPromedioPropiedad(propiedadId);
propiedad.setPuntuacionPromedio(promedio);
propiedadRepository.save(propiedad);
```

---

## 📋 Validaciones de Datos

### CrearValoracionRequest

- `puntuacion`: @NotNull, @Min(1), @Max(5)
- `comentario`: @Size(min=10, max=2000), opcional

### ResponderValoracionRequest

- `respuesta`: @NotNull, @Size(min=10, max=1000)

---

## 🧪 Testing

**Documento:** `docs/TESTING_VALORACIONES.md`

**Escenarios cubiertos:**
1. ✅ Flujo completo de valoración (crear → responder → ver)
2. ✅ Validaciones de seguridad (permisos, estados)
3. ✅ Validaciones de datos (puntuación, longitud)
4. ✅ Múltiples valoraciones (estadísticas)
5. ✅ Búsqueda por puntuación

**Ejemplos incluidos:**
- 9 comandos curl con tokens
- Casos de error esperados
- Verificación de actualización automática
- Ejemplos de Postman/Swagger

---

## 🎨 Impacto en UX

### Búsqueda de Propiedades

```java
// Filtro de puntuación mínima
GET /api/propiedades/buscar?puntuacionMinima=4.5

// Ordenamiento por puntuación
GET /api/propiedades/buscar?ordenarPor=puntuacion
```

### Detalle de Propiedad

```
Propiedad: Casa en la Playa
★★★★★ 4.8 (127 valoraciones)

Distribución:
5 estrellas ████████████████████ 70%
4 estrellas ████████             20%
3 estrellas ███                  6%
2 estrellas █                    2%
1 estrella  █                    2%
```

### Dashboard del Anfitrión

```
Valoraciones pendientes de respuesta: 3
- María López - ★★★★★ - Hace 2 días
- Juan Pérez - ★★★★☆ - Hace 5 días
- Ana García - ★★★★★ - Hace 1 semana
```

---

## 📈 Métricas del Sistema

### KPIs Calculables

- **Tasa de valoración:** % de reservas completadas con valoración
- **Puntuación promedio general:** Promedio de todas las propiedades
- **Tiempo de respuesta:** Media de tiempo entre valoración y respuesta
- **Distribución de puntuaciones:** % por cada estrella

### Queries de Análisis

```sql
-- Propiedades mejor valoradas
SELECT p.titulo, p.puntuacion_promedio, COUNT(v.id) as total
FROM propiedad p
LEFT JOIN valoracion v ON v.propiedad_id = p.id
WHERE v.visible = true
GROUP BY p.id
ORDER BY p.puntuacion_promedio DESC
LIMIT 10;

-- Anfitriones con mejor reputación
SELECT u.nombre, AVG(v.puntuacion) as promedio, COUNT(v.id) as total
FROM usuario u
INNER JOIN valoracion v ON v.valorado_id = u.id
WHERE u.rol = 'ANFITRION' AND v.visible = true
GROUP BY u.id
ORDER BY promedio DESC
LIMIT 10;
```

---

## 🔄 Flujo de Usuario

### Ciclo de Vida de una Valoración

```
1. Viajero completa reserva
   ↓
2. Sistema cambia estado a COMPLETADA
   ↓
3. Checkout (fecha pasa)
   ↓
4. Viajero puede crear valoración
   GET /api/valoraciones/reserva/{id}/puede-valorar → true
   ↓
5. Viajero crea valoración (1-5 estrellas + comentario)
   POST /api/valoraciones/reserva/{id}
   ↓
6. Sistema guarda y actualiza propiedad.puntuacionPromedio
   ↓
7. Valoración visible públicamente
   GET /api/valoraciones/propiedad/{id}
   ↓
8. Anfitrión recibe notificación (TODO: email)
   ↓
9. Anfitrión ve valoración pendiente
   GET /api/valoraciones/pendientes-respuesta
   ↓
10. Anfitrión responde
    PUT /api/valoraciones/{id}/responder
    ↓
11. Respuesta visible junto con valoración
```

---

## 🚀 Próximos Pasos

### Paso 11: Panel de Administración Básico

**Requisitos:**
- Dashboard con métricas generales
- Gestión de usuarios (listar, banear, eliminar)
- Gestión de propiedades (aprobar, rechazar, ocultar)
- Gestión de reservas (supervisar, cancelar)
- Moderación de valoraciones (ocultar/mostrar)
- Todos los endpoints con rol `ADMIN`

**Endpoints a crear:**
- GET `/api/admin/dashboard` - Métricas generales
- GET `/api/admin/usuarios` - Listar usuarios
- PUT `/api/admin/usuarios/{id}/banear` - Banear usuario
- DELETE `/api/admin/usuarios/{id}` - Eliminar usuario
- GET `/api/admin/propiedades` - Listar propiedades
- PUT `/api/admin/propiedades/{id}/aprobar` - Aprobar propiedad
- PUT `/api/admin/propiedades/{id}/rechazar` - Rechazar propiedad
- GET `/api/admin/reservas` - Supervisar reservas
- PUT `/api/admin/valoraciones/{id}/ocultar` - Moderar valoración

---

## 📚 Recursos Generados

1. **Migración:** `V12__create_valoracion_table.sql`
2. **Entidad:** `domain/entity/Valoracion.java`
3. **Repositorio:** `domain/repository/ValoracionRepository.java`
4. **DTOs:** 4 archivos en `dto/valoracion/`
5. **Servicio:** `service/ValoracionService.java`
6. **Controlador:** `controller/ValoracionController.java`
7. **Seguridad:** `config/SecurityConfig.java` (actualizado)
8. **Testing:** `docs/TESTING_VALORACIONES.md`
9. **Resumen:** `docs/RESUMEN_PASO_10.md`

---

## 🎉 Logros Destacados

✅ **Sistema completo de reputación** con valoraciones y respuestas  
✅ **Actualización automática** de puntuaciones promedio  
✅ **Estadísticas detalladas** con distribución por estrellas  
✅ **Validaciones robustas** (12 reglas de negocio)  
✅ **Endpoints públicos y privados** correctamente configurados  
✅ **Una valoración por reserva** (constraint UNIQUE)  
✅ **Respuestas de anfitriones** con timestamp  
✅ **Paginación** en todas las consultas de listado  
✅ **Índices optimizados** (9 índices para rendimiento)  
✅ **Documentación completa** con ejemplos de testing  

---

**Progreso general:** 50% del proyecto (10/20 pasos) ✨
