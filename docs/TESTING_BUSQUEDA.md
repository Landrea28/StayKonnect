# Guía de Testing - Búsqueda y Filtrado de Propiedades

Esta guía describe cómo probar el sistema de búsqueda avanzada de propiedades.

## Prerrequisitos

1. Backend corriendo en `http://localhost:8080`
2. Base de datos con propiedades de prueba (ver `V4_test_data.sql`)
3. Usuario autenticado (opcional, el endpoint es público)

## Endpoint de Búsqueda

**URL:** `GET /api/propiedades/buscar`

**Parámetros disponibles (todos opcionales):**

| Parámetro | Tipo | Descripción | Ejemplo |
|-----------|------|-------------|---------|
| `query` | String | Busca en título y descripción | `villa`, `playa` |
| `ciudad` | String | Filtra por ciudad | `Cartagena`, `Bogotá` |
| `pais` | String | Filtra por país | `Colombia`, `España` |
| `fechaInicio` | LocalDate | Fecha inicio disponibilidad | `2024-03-01` |
| `fechaFin` | LocalDate | Fecha fin disponibilidad | `2024-03-15` |
| `tipoPropiedad` | Enum | Tipo de propiedad | `CASA`, `APARTAMENTO`, `VILLA` |
| `capacidadMinima` | Integer | Capacidad mínima huéspedes | `4`, `6` |
| `habitacionesMinimas` | Integer | Habitaciones mínimas | `2`, `3` |
| `camasMinimas` | Integer | Camas mínimas | `2`, `4` |
| `banosMinimos` | Integer | Baños mínimos | `1`, `2` |
| `precioMinimo` | BigDecimal | Precio mínimo por noche | `50000`, `100000` |
| `precioMaximo` | BigDecimal | Precio máximo por noche | `200000`, `500000` |
| `servicios` | List<String> | Servicios requeridos (AND) | `WIFI,PISCINA` |
| `puntuacionMinima` | Double | Puntuación mínima | `4.0`, `4.5` |
| `ordenarPor` | String | Campo de ordenamiento | `precio`, `puntuacion`, `reciente`, `relevancia` |
| `direccion` | String | Dirección ordenamiento | `asc`, `desc` |
| `page` | Integer | Número de página | `0`, `1` |
| `size` | Integer | Tamaño de página | `10`, `20` |

## Casos de Prueba

### 1. Búsqueda Simple por Ciudad

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?ciudad=Cartagena" | jq
```

**Respuesta esperada:**
- Todas las propiedades activas en Cartagena
- Estado HTTP: 200 OK

### 2. Búsqueda por Texto

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?query=villa" | jq
```

**Respuesta esperada:**
- Propiedades con "villa" en título o descripción
- Case-insensitive

### 3. Filtro por Rango de Precios

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?precioMinimo=100000&precioMaximo=300000" | jq
```

**Respuesta esperada:**
- Propiedades con precio entre 100,000 y 300,000 COP por noche

### 4. Filtro por Capacidad y Habitaciones

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?capacidadMinima=6&habitacionesMinimas=3" | jq
```

**Respuesta esperada:**
- Propiedades con capacidad >= 6 huéspedes
- Con >= 3 habitaciones

### 5. Filtro por Servicios (AND)

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?servicios=WIFI&servicios=PISCINA&servicios=AIRE_ACONDICIONADO" | jq
```

**Respuesta esperada:**
- Solo propiedades que tienen TODOS los servicios especificados
- WiFi AND Piscina AND Aire Acondicionado

### 6. Filtro por Disponibilidad en Fechas

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-06-01&fechaFin=2024-06-15" | jq
```

**Respuesta esperada:**
- Propiedades sin reservas confirmadas/pagadas/en curso en ese rango
- Considera solapamiento de fechas

### 7. Búsqueda Compleja Combinada

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?\
ciudad=Cartagena&\
tipoPropiedad=VILLA&\
capacidadMinima=6&\
precioMinimo=150000&\
precioMaximo=400000&\
servicios=WIFI&servicios=PISCINA&\
puntuacionMinima=4.5&\
ordenarPor=precio&\
direccion=asc&\
page=0&\
size=10" | jq
```

**Respuesta esperada:**
- Villas en Cartagena
- Para 6+ personas
- Entre 150K-400K por noche
- Con WiFi y Piscina
- Puntuación >= 4.5
- Ordenadas por precio ascendente

### 8. Ordenamiento por Puntuación

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?ordenarPor=puntuacion&direccion=desc&size=5" | jq
```

**Respuesta esperada:**
- Top 5 propiedades mejor puntuadas
- Orden descendente

### 9. Ordenamiento por Precio

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?ciudad=Bogota&ordenarPor=precio&direccion=asc" | jq
```

**Respuesta esperada:**
- Propiedades en Bogotá ordenadas por precio ascendente (más baratas primero)

### 10. Ordenamiento por Relevancia

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?query=lujo&ordenarPor=relevancia" | jq
```

**Respuesta esperada:**
- Propiedades con "lujo" ordenadas por totalValoraciones y puntuación

### 11. Búsqueda Sin Resultados

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?capacidadMinima=100" | jq
```

**Respuesta esperada:**
- Array vacío
- `totalElements: 0`
- Estado HTTP: 200 OK

### 12. Paginación

```bash
# Primera página
curl -X GET "http://localhost:8080/api/propiedades/buscar?page=0&size=2" | jq

# Segunda página
curl -X GET "http://localhost:8080/api/propiedades/buscar?page=1&size=2" | jq
```

**Verificar:**
- Diferentes propiedades en cada página
- Metadata: `totalElements`, `totalPages`, `number`, `size`

## Validaciones de Negocio

### Disponibilidad por Fechas

**Escenario:** Propiedad con reserva confirmada del 2024-06-01 al 2024-06-10

```bash
# ❌ NO debe aparecer (solapa completamente)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-06-01&fechaFin=2024-06-10"

# ❌ NO debe aparecer (solapa inicio)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-05-25&fechaFin=2024-06-05"

# ❌ NO debe aparecer (solapa fin)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-06-05&fechaFin=2024-06-15"

# ❌ NO debe aparecer (contenida en reserva)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-06-03&fechaFin=2024-06-07"

# ✅ SÍ debe aparecer (antes de reserva)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-05-15&fechaFin=2024-05-31"

# ✅ SÍ debe aparecer (después de reserva)
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024-06-11&fechaFin=2024-06-20"
```

### Estados de Propiedades

Solo propiedades con `estado = ACTIVA` deben aparecer en los resultados.

```bash
# Verificar que NO aparezcan propiedades INACTIVA, PENDIENTE_APROBACION, BLOQUEADA, ELIMINADA
curl -X GET "http://localhost:8080/api/propiedades/buscar" | jq '.data.content[].estado'
```

**Todos los resultados deben ser:** `"ACTIVA"`

## Pruebas de Rendimiento

### Objetivo: < 2 segundos por búsqueda

```bash
# Búsqueda simple
time curl -X GET "http://localhost:8080/api/propiedades/buscar?ciudad=Cartagena"

# Búsqueda compleja
time curl -X GET "http://localhost:8080/api/propiedades/buscar?\
ciudad=Cartagena&capacidadMinima=4&servicios=WIFI&servicios=PISCINA"
```

**Verificar:**
- Tiempo total < 2 segundos
- Logs en backend: "Búsqueda completada. Resultados encontrados: X"

### Análisis de Queries SQL (opcional)

Si tienes acceso a la base de datos:

```sql
-- Habilitar logs de queries en PostgreSQL
SET log_min_duration_statement = 0;

-- Ejecutar búsqueda desde la app
-- Revisar logs de PostgreSQL para ver el SQL generado
```

**Verificar:**
- Uso de índices en columnas: ciudad, pais, precio_por_noche, capacidad, tipo_propiedad
- No full table scans innecesarios

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Propiedades"** en el listado
2. Expande `GET /api/propiedades/buscar`
3. Haz clic en **"Try it out"**
4. Configura los filtros deseados
5. Haz clic en **"Execute"**

**Ventajas:**
- Interfaz visual
- Autocompletado de valores enum
- Formato de respuesta legible
- No requiere curl

## Estructura de Respuesta

```json
{
  "success": true,
  "message": "Se encontraron 3 propiedades",
  "data": {
    "content": [
      {
        "id": 1,
        "titulo": "Villa de Lujo Frente al Mar",
        "tipoPropiedad": "VILLA",
        "ciudad": "Cartagena",
        "pais": "Colombia",
        "habitaciones": 4,
        "camas": 6,
        "banos": 3,
        "capacidad": 8,
        "precioPorNoche": 350000,
        "imagenPrincipal": "http://localhost:8080/images/villa-uuid.jpg",
        "estado": "ACTIVA",
        "puntuacionPromedio": 4.8,
        "totalValoraciones": 25,
        "anfitrionNombre": "Carlos Mendoza"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": { "sorted": true, "unsorted": false },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 3,
    "totalPages": 1,
    "last": true,
    "size": 20,
    "number": 0,
    "first": true,
    "numberOfElements": 3,
    "empty": false
  }
}
```

## Errores Comunes

### 400 Bad Request - Fecha inválida

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?fechaInicio=2024/03/01"
# ❌ Formato incorrecto. Usar: 2024-03-01 (ISO-8601)
```

### 400 Bad Request - Tipo de propiedad inválido

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar?tipoPropiedad=MANSION"
# ❌ Valores válidos: APARTAMENTO, CASA, VILLA, CABANA, HOTEL, HOSTAL
```

### Búsqueda sin parámetros

```bash
curl -X GET "http://localhost:8080/api/propiedades/buscar" | jq
# ✅ Devuelve todas las propiedades activas (mismo comportamiento que GET /propiedades)
```

## Propiedades de Prueba Disponibles

Ver archivo `V4_test_data.sql` para las 4 propiedades precargadas:

1. **Villa de Lujo Frente al Mar** (Cartagena, $350K, 4 hab, 8 personas)
2. **Apartamento Moderno en Chapinero** (Bogotá, $120K, 2 hab, 4 personas)
3. **Casa Campestre en Cajicá** (Cajicá, $180K, 3 hab, 6 personas)
4. **Apartaestudio en El Poblado** (Medellín, $90K, 1 hab, 2 personas)

## Notas Técnicas

- **Case-insensitive:** Búsquedas de texto no distinguen mayúsculas
- **Filtros AND:** Todos los filtros se combinan con AND lógico
- **Servicios requeridos:** La propiedad debe tener TODOS los servicios listados
- **Disponibilidad:** Valida reservas en estados CONFIRMADA, PAGADA, EN_CURSO
- **Paginación:** Default 20 elementos por página, máximo configurable
- **Ordenamiento múltiple:** Relevancia = totalValoraciones DESC, puntuacionPromedio DESC

## Integración con Frontend

```typescript
// Ejemplo Angular
buscarPropiedades(filtros: BusquedaFiltros): Observable<Page<PropiedadResumen>> {
  const params = new HttpParams({ fromObject: filtros });
  return this.http.get<ApiResponse<Page<PropiedadResumen>>>(
    `${API_URL}/propiedades/buscar`,
    { params }
  ).pipe(map(response => response.data));
}
```

```javascript
// Ejemplo React
const buscarPropiedades = async (filtros) => {
  const queryString = new URLSearchParams(filtros).toString();
  const response = await fetch(`${API_URL}/propiedades/buscar?${queryString}`);
  const data = await response.json();
  return data.data;
};
```

## Logs del Backend

Al ejecutar una búsqueda, verifica los logs:

```
INFO  c.s.service.BusquedaPropiedadService : Buscando propiedades con filtros: BusquedaPropiedadRequest(query=villa, ciudad=Cartagena, ...)
INFO  c.s.service.BusquedaPropiedadService : Búsqueda completada. Resultados encontrados: 2
```

## Próximos Pasos

Una vez validado el sistema de búsqueda, continuar con:

- **Paso 7:** Sistema de reservas (RF04)
- **Paso 8:** Pasarela de pago (RF05)
