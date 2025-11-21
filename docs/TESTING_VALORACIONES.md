# Guía de Testing - Sistema de Reputación y Valoraciones

Esta guía describe cómo probar el sistema de valoraciones y reputación.

## Prerrequisitos

1. **Backend corriendo** en `http://localhost:8080`
2. **Reserva completada** con estado `COMPLETADA` y checkout en el pasado
3. **Dos usuarios**: Viajero (quien valora) y Anfitrión (quien es valorado)

## Flujo de Valoración

### Ciclo de Vida de una Valoración

```
1. Reserva COMPLETADA + Checkout pasado
   ↓
2. Viajero puede valorar
   ↓
3. Viajero crea valoración (puntuación 1-5 + comentario)
   ↓
4. Valoración visible públicamente
   ↓
5. Anfitrión recibe notificación
   ↓
6. Anfitrión responde (opcional)
   ↓
7. Actualización automática de puntuación promedio
```

## Configuración Inicial

### 1. Autenticar Usuarios

```bash
# Viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.viajera@test.com","password":"Test1234@"}' | jq -r '.data.token'

export TOKEN_VIAJERO="tu_token_aqui"

# Anfitrión
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"carlos.anfitrion@test.com","password":"Test1234@"}' | jq -r '.data.token'

export TOKEN_ANFITRION="tu_token_aqui"
```

### 2. Preparar Reserva Completada

Para poder valorar, necesitas una reserva con:
- Estado: `COMPLETADA`
- Fecha checkout en el pasado

```bash
# Verificar estado de reserva
curl -X GET http://localhost:8080/api/reservas/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq '.data | {id, estado, fechaCheckout}'

# Ejemplo de salida esperada:
# {
#   "id": 1,
#   "estado": "COMPLETADA",
#   "fechaCheckout": "2024-11-15T12:00:00"
# }
```

## Testing de Endpoints REST

### Endpoint 1: Verificar si Puede Valorar (GET /api/valoraciones/reserva/{reservaId}/puede-valorar)

```bash
# Verificar si el viajero puede valorar la reserva
curl -X GET http://localhost:8080/api/valoraciones/reserva/1/puede-valorar \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Puedes valorar esta reserva",
  "data": true
}
```

**Validaciones:**
- ✅ Usuario es el viajero de la reserva
- ✅ Reserva está en estado COMPLETADA
- ✅ Fecha checkout ya pasó
- ✅ No existe valoración previa

### Endpoint 2: Crear Valoración (POST /api/valoraciones/reserva/{reservaId})

```bash
# Crear valoración (SOLO VIAJERO)
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "puntuacion": 5,
    "comentario": "Excelente propiedad! Todo estaba impecable y el anfitrión fue muy amable. La ubicación es perfecta y las instalaciones superaron mis expectativas. Definitivamente regresaría!"
  }' | jq
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Valoración creada exitosamente. ¡Gracias por tu opinión!",
  "data": {
    "id": 1,
    "puntuacion": 5,
    "comentario": "Excelente propiedad! Todo estaba impecable...",
    "respuestaAnfitrion": null,
    "fechaRespuesta": null,
    "visible": true,
    "createdDate": "2024-11-19T16:00:00",
    "reservaId": 1,
    "fechaCheckin": "2024-11-10T14:00:00",
    "fechaCheckout": "2024-11-15T12:00:00",
    "valoradorId": 1,
    "valoradorNombre": "María López",
    "valoradorFotoPerfil": null,
    "valoradoId": 2,
    "valoradoNombre": "Carlos García",
    "valoradoFotoPerfil": null,
    "propiedadId": 1,
    "propiedadTitulo": "Departamento céntrico con vista al mar",
    "propiedadImagenPrincipal": "/images/prop1_main.jpg"
  }
}
```

**Efectos secundarios:**
- ✅ Valoración guardada en BD
- ✅ Puntuación promedio de propiedad actualizada
- ✅ Valoración visible públicamente
- ✅ Anfitrión puede ver la valoración

### Endpoint 3: Obtener Valoraciones de Propiedad (GET /api/valoraciones/propiedad/{propiedadId})

```bash
# Obtener valoraciones de una propiedad (PÚBLICO)
curl -X GET "http://localhost:8080/api/valoraciones/propiedad/1?page=0&size=20" | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Valoraciones obtenidas exitosamente",
  "data": {
    "content": [
      {
        "id": 1,
        "puntuacion": 5,
        "comentario": "Excelente propiedad!...",
        "respuestaAnfitrion": null,
        "fechaRespuesta": null,
        "visible": true,
        "createdDate": "2024-11-19T16:00:00",
        "valoradorNombre": "María López",
        "propiedadTitulo": "Departamento céntrico con vista al mar"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

### Endpoint 4: Obtener Estadísticas de Propiedad (GET /api/valoraciones/propiedad/{propiedadId}/estadisticas)

```bash
# Obtener estadísticas detalladas (PÚBLICO)
curl -X GET http://localhost:8080/api/valoraciones/propiedad/1/estadisticas | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Estadísticas obtenidas exitosamente",
  "data": {
    "promedioGeneral": 4.6,
    "totalValoraciones": 50,
    "distribucionPuntuaciones": {
      "5": 35,
      "4": 10,
      "3": 3,
      "2": 1,
      "1": 1
    },
    "porcentaje5Estrellas": 70.0,
    "porcentaje4Estrellas": 20.0,
    "porcentaje3Estrellas": 6.0,
    "porcentaje2Estrellas": 2.0,
    "porcentaje1Estrella": 2.0
  }
}
```

### Endpoint 5: Responder Valoración (PUT /api/valoraciones/{valoracionId}/responder)

```bash
# Anfitrión responde a valoración (SOLO ANFITRION)
curl -X PUT http://localhost:8080/api/valoraciones/1/responder \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{
    "respuesta": "¡Muchas gracias María por tu valoración! Fue un placer tenerte como huésped. Espero verte pronto de nuevo!"
  }' | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Respuesta publicada exitosamente",
  "data": {
    "id": 1,
    "puntuacion": 5,
    "comentario": "Excelente propiedad!...",
    "respuestaAnfitrion": "¡Muchas gracias María por tu valoración!...",
    "fechaRespuesta": "2024-11-19T16:30:00",
    "visible": true,
    "createdDate": "2024-11-19T16:00:00"
  }
}
```

### Endpoint 6: Obtener Valoraciones Pendientes de Respuesta (GET /api/valoraciones/pendientes-respuesta)

```bash
# Anfitrión ve valoraciones sin responder (SOLO ANFITRION)
curl -X GET "http://localhost:8080/api/valoraciones/pendientes-respuesta?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

**Uso:** Dashboard del anfitrión para gestionar respuestas

### Endpoint 7: Obtener Mis Valoraciones (GET /api/valoraciones/mis-valoraciones)

```bash
# Viajero ve sus valoraciones realizadas
curl -X GET "http://localhost:8080/api/valoraciones/mis-valoraciones?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

### Endpoint 8: Obtener Valoraciones de Anfitrión (GET /api/valoraciones/anfitrion/{anfitrionId})

```bash
# Ver todas las valoraciones de un anfitrión (PÚBLICO)
curl -X GET "http://localhost:8080/api/valoraciones/anfitrion/2?page=0&size=20" | jq
```

**Uso:** Perfil público del anfitrión

### Endpoint 9: Obtener Valoración por Reserva (GET /api/valoraciones/reserva/{reservaId})

```bash
# Ver valoración de una reserva específica
curl -X GET http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

**Uso:** Detalle de reserva con su valoración

## Casos de Prueba

### Escenario 1: Flujo Completo de Valoración

```bash
# 1. Verificar que puede valorar
curl -X GET http://localhost:8080/api/valoraciones/reserva/1/puede-valorar \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq '.data'
# Output: true

# 2. Crear valoración
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 5, "comentario": "Excelente estadía!"}'

# 3. Ver estadísticas actualizadas
curl -X GET http://localhost:8080/api/valoraciones/propiedad/1/estadisticas | jq

# 4. Anfitrión ve valoración pendiente
curl -X GET http://localhost:8080/api/valoraciones/pendientes-respuesta \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq

# 5. Anfitrión responde
curl -X PUT http://localhost:8080/api/valoraciones/1/responder \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{"respuesta": "¡Gracias por tu valoración!"}'

# 6. Viajero ve la respuesta
curl -X GET http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

### Escenario 2: Validaciones de Seguridad

```bash
# ❌ Error: Intentar valorar reserva de otro usuario
curl -X POST http://localhost:8080/api/valoraciones/reserva/999 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 5, "comentario": "Test"}'
# Error: "Solo el viajero de la reserva puede valorar"

# ❌ Error: Valorar reserva no completada
curl -X POST http://localhost:8080/api/valoraciones/reserva/2 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 5, "comentario": "Test"}'
# Error: "Solo se pueden valorar reservas completadas"

# ❌ Error: Doble valoración
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 4, "comentario": "Otra valoración"}'
# Error: "Esta reserva ya ha sido valorada"

# ❌ Error: Anfitrión responde a valoración de otro
curl -X PUT http://localhost:8080/api/valoraciones/999/responder \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{"respuesta": "Test"}'
# Error: "Solo el anfitrión puede responder esta valoración"

# ❌ Error: Doble respuesta
curl -X PUT http://localhost:8080/api/valoraciones/1/responder \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{"respuesta": "Segunda respuesta"}'
# Error: "Esta valoración ya tiene una respuesta"
```

### Escenario 3: Validaciones de Datos

```bash
# ❌ Error: Puntuación inválida (menor a 1)
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 0, "comentario": "Mal"}'
# Error: "La puntuación mínima es 1"

# ❌ Error: Puntuación inválida (mayor a 5)
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 6, "comentario": "Excelente"}'
# Error: "La puntuación máxima es 5"

# ❌ Error: Comentario muy corto
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 5, "comentario": "Bien"}'
# Error: "El comentario debe tener entre 10 y 2000 caracteres"

# ❌ Error: Respuesta muy corta
curl -X PUT http://localhost:8080/api/valoraciones/1/responder \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{"respuesta": "Ok"}'
# Error: "La respuesta debe tener entre 10 y 1000 caracteres"
```

### Escenario 4: Múltiples Valoraciones (Estadísticas)

```bash
# Crear valoraciones con diferentes puntuaciones
for puntuacion in 5 5 5 5 4 4 3 2; do
  # Nota: Necesitas diferentes reservas completadas
  curl -X POST http://localhost:8080/api/valoraciones/reserva/$reserva_id \
    -H "Authorization: Bearer $TOKEN_VIAJERO" \
    -H "Content-Type: application/json" \
    -d "{\"puntuacion\": $puntuacion, \"comentario\": \"Comentario de prueba con al menos 10 caracteres\"}"
  sleep 1
done

# Ver distribución de puntuaciones
curl -X GET http://localhost:8080/api/valoraciones/propiedad/1/estadisticas | jq
```

### Escenario 5: Búsqueda de Propiedades por Puntuación

```bash
# Buscar propiedades con puntuación mínima 4.5
curl -X GET "http://localhost:8080/api/propiedades/buscar?puntuacionMinima=4.5" | jq
```

## Integración con Otros Sistemas

### Actualización Automática de Puntuación Promedio

Cuando se crea una valoración, el sistema automáticamente:

```java
// 1. Calcula nuevo promedio
Double promedio = valoracionRepository.calcularPromedioPropiedad(propiedadId);

// 2. Actualiza propiedad
propiedad.setPuntuacionPromedio(promedio);

// 3. Guarda cambios
propiedadRepository.save(propiedad);
```

**Verificar actualización:**
```bash
# Antes de valorar
curl -X GET http://localhost:8080/api/propiedades/1 | jq '.data.puntuacionPromedio'
# Output: 4.5

# Crear valoración de 5 estrellas
curl -X POST http://localhost:8080/api/valoraciones/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"puntuacion": 5, "comentario": "Excelente!!!!!"}'

# Después de valorar
curl -X GET http://localhost:8080/api/propiedades/1 | jq '.data.puntuacionPromedio'
# Output: 4.6 (promedio actualizado)
```

### Impacto en Búsqueda

Las valoraciones afectan:
1. **Ordenamiento por relevancia:** Propiedades con mejor puntuación aparecen primero
2. **Filtro de puntuación mínima:** Solo propiedades que cumplan el mínimo
3. **Estadísticas públicas:** Visibles en detalle de propiedad

## UI/UX Recomendado

### Componente de Estrellas

```html
<!-- Ejemplo de visualización -->
★★★★★ 4.8 (127 valoraciones)

<!-- Distribución en detalle -->
5 estrellas ████████████████████ 70%
4 estrellas ████████             20%
3 estrellas ███                  6%
2 estrellas █                    2%
1 estrella  █                    2%
```

### Card de Valoración

```
┌─────────────────────────────────────────┐
│ ★★★★★ María López                      │
│ Hace 2 días                             │
│                                         │
│ Excelente propiedad! Todo estaba       │
│ impecable y el anfitrión fue muy       │
│ amable...                               │
│                                         │
│ └─ Respuesta de Carlos García:         │
│    ¡Muchas gracias María! Fue un       │
│    placer tenerte como huésped.        │
│    Hace 1 día                           │
└─────────────────────────────────────────┘
```

## Testing con Postman

1. **Importar colección** con 9 endpoints
2. **Configurar variables:**
   - `base_url`: http://localhost:8080
   - `token_viajero`: JWT del viajero
   - `token_anfitrion`: JWT del anfitrión
   - `reserva_id`: ID de reserva completada
   - `propiedad_id`: ID de propiedad
   - `valoracion_id`: ID de valoración

3. **Ejecutar en orden:**
   - Puede valorar? → Crear → Ver estadísticas → Responder → Ver respuesta

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Valoraciones"**
2. Autoriza con tu JWT
3. Prueba endpoints en orden lógico

## Métricas y Análisis

### KPIs del Sistema de Reputación

- **Tasa de valoración:** % de reservas completadas que son valoradas
- **Puntuación promedio general:** Promedio de todas las propiedades
- **Tiempo de respuesta:** Tiempo entre valoración y respuesta del anfitrión
- **Distribución de puntuaciones:** % por estrella (idealmente >70% en 4-5)

### Queries de Análisis

```bash
# Propiedades mejor valoradas
curl -X GET "http://localhost:8080/api/propiedades/buscar?ordenarPor=puntuacion" | jq

# Anfitriones con mejor reputación
# (Implementar endpoint específico en futuro)
```

## Moderación (Futuro)

El campo `visible` permite ocultar valoraciones inapropiadas:

```sql
-- Ocultar valoración
UPDATE valoracion SET visible = false WHERE id = 123;

-- Estadísticas solo cuentan valoraciones visibles
SELECT AVG(puntuacion) FROM valoracion 
WHERE propiedad_id = 1 AND visible = true;
```

## Próximos Pasos

Una vez validado el sistema de reputación:

- **Paso 11:** Panel de administración (gestión de usuarios, propiedades, moderación)
- **Paso 12:** Sistema de notificaciones (email al anfitrión cuando recibe valoración)
- **Paso 13:** Reportes y métricas avanzadas

## Recursos Adicionales

- [Best Practices for Review Systems](https://www.nngroup.com/articles/rating-reviews/)
- [Trust and Safety in Marketplaces](https://www.airbnb.com/trust)
