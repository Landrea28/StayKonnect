# Testing - Panel Avanzado de Reportes (Paso 12)

## Configuración Inicial

### Datos de Prueba
Asegúrate de tener datos de prueba en la base de datos:
- Varias reservas en diferentes estados
- Reservas en diferentes períodos (meses, trimestres)
- Múltiples propiedades con diferentes ocupaciones
- Varios anfitriones con diferentes niveles de actividad

### Autenticación
Todos los endpoints requieren autenticación con rol ADMIN.

```bash
# Login como admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@staykonnect.com",
    "password": "Admin123!"
  }'

# Guardar el token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Ejemplos de Testing

### 1. Reporte de Ingresos

```bash
# Obtener ingresos mensuales del último trimestre
curl -X GET "http://localhost:8080/api/reportes/ingresos?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&periodo=MENSUAL" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "fecha": "2024-10-01",
    "periodo": "octubre 2024",
    "ingresosBrutos": 15000000.00,
    "comisiones": 1500000.00,
    "ingresosNetos": 13500000.00,
    "numeroReservas": 25,
    "ingresoPorReserva": 600000.00,
    "tasaCrecimiento": 15.50
  },
  {
    "fecha": "2024-11-01",
    "periodo": "noviembre 2024",
    "ingresosBrutos": 18000000.00,
    "comisiones": 1800000.00,
    "ingresosNetos": 16200000.00,
    "numeroReservas": 30,
    "ingresoPorReserva": 600000.00,
    "tasaCrecimiento": 20.00
  }
]
```

### 2. Reporte de Ocupación

```bash
# Obtener ocupación de todas las propiedades en el último mes
curl -X GET "http://localhost:8080/api/reportes/ocupacion?fechaInicio=2024-11-01T00:00:00&fechaFin=2024-11-30T23:59:59" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "propiedadId": 1,
    "propiedadNombre": "Apartamento Moderno en Chapinero",
    "propiedadCiudad": "Bogotá",
    "propiedadTipo": "APARTAMENTO",
    "diasDisponibles": 30,
    "diasReservados": 21,
    "diasBloqueados": 0,
    "tasaOcupacion": 70.00,
    "numeroReservas": 3,
    "ingresosGenerados": 2100000.00,
    "ingresoPorDiaReservado": 100000.00,
    "puntuacionPromedio": 4.5
  }
]
```

### 3. Reporte de Comisiones

```bash
# Obtener comisiones mensuales
curl -X GET "http://localhost:8080/api/reportes/comisiones?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&periodo=MENSUAL" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "fecha": "2024-10-01",
    "periodo": "octubre 2024",
    "comisionesGeneradas": 1500000.00,
    "comisionesReales": 1500000.00,
    "comisionesPendientes": 0.00,
    "numeroTransacciones": 25,
    "comisionPromedio": 60000.00,
    "porcentajeComisionPromedio": 10.00,
    "ingresosTotales": 15000000.00
  }
]
```

### 4. Top Propiedades

```bash
# Obtener top 10 propiedades por ingresos
curl -X GET "http://localhost:8080/api/reportes/top-propiedades?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&limite=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "propiedadId": 5,
    "nombre": "Casa de Playa en Cartagena",
    "ciudad": "Cartagena",
    "tipo": "CASA",
    "anfitrionId": 3,
    "anfitrionNombre": "María González",
    "ingresosGenerados": 5000000.00,
    "numeroReservas": 8,
    "tasaOcupacion": 85.50,
    "puntuacionPromedio": 4.8,
    "numeroValoraciones": 7,
    "precioPromedioPorNoche": 750000.00,
    "posicion": 1
  }
]
```

### 5. Top Anfitriones

```bash
# Obtener top 10 anfitriones
curl -X GET "http://localhost:8080/api/reportes/top-anfitriones?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&limite=10" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "anfitrionId": 3,
    "nombre": "María González",
    "email": "maria.gonzalez@example.com",
    "numeroPropiedades": 3,
    "propiedadesActivas": 3,
    "ingresosGenerados": 12000000.00,
    "numeroReservasTotales": 20,
    "numeroReservasCompletadas": 18,
    "tasaCompletamiento": 90.00,
    "puntuacionPromedio": 4.7,
    "numeroValoracionesRecibidas": 15,
    "posicion": 1
  }
]
```

### 6. Análisis de Estacionalidad

```bash
# Obtener estacionalidad del año 2024
curl -X GET "http://localhost:8080/api/reportes/estacionalidad?anio=2024" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada:**
```json
[
  {
    "mes": 1,
    "nombreMes": "Enero",
    "temporada": "ALTA",
    "numeroReservas": 45,
    "ingresosGenerados": 18000000.00,
    "tasaOcupacionPromedio": 75.00,
    "precioPromedioPorNoche": 550000.00,
    "numeroViajeros": 38,
    "tasaCrecimientoAnual": 0.00
  },
  {
    "mes": 7,
    "nombreMes": "Julio",
    "temporada": "ALTA",
    "numeroReservas": 50,
    "ingresosGenerados": 20000000.00,
    "tasaOcupacionPromedio": 80.00,
    "precioPromedioPorNoche": 600000.00,
    "numeroViajeros": 42,
    "tasaCrecimientoAnual": 0.00
  }
]
```

### 7. Datos para Gráficos

```bash
# Gráfico de ingresos
curl -X GET "http://localhost:8080/api/reportes/grafico/ingresos?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&periodo=MENSUAL" \
  -H "Authorization: Bearer $TOKEN"

# Gráfico de ocupación
curl -X GET "http://localhost:8080/api/reportes/grafico/ocupacion?fechaInicio=2024-11-01T00:00:00&fechaFin=2024-11-30T23:59:59" \
  -H "Authorization: Bearer $TOKEN"

# Gráfico de estacionalidad
curl -X GET "http://localhost:8080/api/reportes/grafico/estacionalidad?anio=2024" \
  -H "Authorization: Bearer $TOKEN"
```

**Respuesta esperada (gráfico de ingresos):**
```json
{
  "titulo": "Análisis de Ingresos",
  "tipo": "line",
  "etiquetas": ["octubre 2024", "noviembre 2024", "diciembre 2024"],
  "series": [
    {
      "nombre": "Ingresos Brutos",
      "color": "#10b981",
      "datos": [15000000.00, 18000000.00, 16000000.00]
    },
    {
      "nombre": "Comisiones",
      "color": "#f59e0b",
      "datos": [1500000.00, 1800000.00, 1600000.00]
    },
    {
      "nombre": "Ingresos Netos",
      "color": "#3b82f6",
      "datos": [13500000.00, 16200000.00, 14400000.00]
    }
  ]
}
```

### 8. Exportar a PDF

```bash
# Descargar reporte de ingresos en PDF
curl -X GET "http://localhost:8080/api/reportes/exportar/ingresos/pdf?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&periodo=MENSUAL" \
  -H "Authorization: Bearer $TOKEN" \
  --output reporte-ingresos.pdf
```

### 9. Exportar a Excel

```bash
# Descargar reporte de ingresos en Excel
curl -X GET "http://localhost:8080/api/reportes/exportar/ingresos/excel?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59&periodo=MENSUAL" \
  -H "Authorization: Bearer $TOKEN" \
  --output reporte-ingresos.xlsx
```

## Escenarios de Prueba

### Escenario 1: Análisis Trimestral Completo
1. Generar reporte de ingresos mensual del Q4 2024
2. Generar reporte de ocupación del mismo período
3. Generar reporte de comisiones
4. Comparar tasas de crecimiento
5. Verificar coherencia entre reportes (ingresos vs comisiones)

### Escenario 2: Identificar Mejores Propiedades
1. Obtener top 20 propiedades por ingresos
2. Cruzar con reporte de ocupación
3. Verificar que las propiedades con mayor ocupación estén en el top
4. Revisar puntuaciones promedio
5. Identificar propiedades a promover

### Escenario 3: Análisis de Estacionalidad
1. Obtener datos de estacionalidad del año completo
2. Identificar temporadas alta, media, baja
3. Generar gráfico de estacionalidad
4. Planear estrategias de precios para temporada baja
5. Comparar con año anterior (si hay datos)

### Escenario 4: Evaluación de Anfitriones
1. Obtener top 50 anfitriones
2. Revisar tasas de completamiento
3. Verificar puntuaciones promedio
4. Identificar anfitriones destacados (para programa de beneficios)
5. Identificar anfitriones con bajo desempeño (para capacitación)

### Escenario 5: Exportación para Junta Directiva
1. Generar reporte de ingresos trimestral en PDF
2. Incluir análisis de crecimiento
3. Exportar datos detallados en Excel
4. Preparar gráficos para presentación
5. Calcular proyecciones basadas en tasas de crecimiento

## Validaciones

### Validaciones de Datos
- ✅ Fechas válidas (inicio < fin)
- ✅ Períodos válidos (DIARIO, SEMANAL, MENSUAL, TRIMESTRAL, ANUAL)
- ✅ Límites razonables (1-100 para tops)
- ✅ Solo datos de reservas completadas o pagadas
- ✅ Cálculos correctos de porcentajes y tasas

### Validaciones de Seguridad
- ✅ Solo usuarios con rol ADMIN pueden acceder
- ✅ Token JWT válido requerido
- ✅ No exponer datos sensibles de usuarios

### Validaciones de Performance
- ✅ Queries optimizadas con índices
- ✅ Paginación cuando sea necesario
- ✅ Timeout máximo de 30 segundos
- ✅ Generación de PDF/Excel en menos de 10 segundos

## Integración con Frontend

### Chart.js
Los datos de gráficos están optimizados para Chart.js:

```javascript
// Ejemplo de integración con Chart.js
fetch('/api/reportes/grafico/ingresos?fechaInicio=2024-10-01T00:00:00&fechaFin=2024-12-31T23:59:59', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(res => res.json())
.then(data => {
  new Chart(ctx, {
    type: data.tipo, // 'line', 'bar', 'pie'
    data: {
      labels: data.etiquetas,
      datasets: data.series.map(s => ({
        label: s.nombre,
        data: s.datos,
        backgroundColor: s.color,
        borderColor: s.color
      }))
    }
  });
});
```

### Descargas de Archivos
```javascript
// Ejemplo de descarga de PDF
async function descargarPDF() {
  const response = await fetch('/api/reportes/exportar/ingresos/pdf?...', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'reporte-ingresos.pdf';
  a.click();
}
```

## Métricas de Performance

### Queries Optimizadas
- Uso de `DATE_TRUNC` para agrupación eficiente
- `COALESCE` para manejar valores NULL
- Índices en campos de fecha
- JOINs optimizados con LEFT JOIN cuando sea necesario

### Tiempos Esperados
- Reportes de ingresos: < 3s
- Reportes de ocupación: < 5s
- Top propiedades/anfitriones: < 2s
- Estacionalidad: < 2s
- Exportación PDF: < 5s
- Exportación Excel: < 8s

## Troubleshooting

### Problema: PDF vacío o corrupto
- Verificar que iText7 esté instalado correctamente
- Verificar permisos de escritura en disco
- Revisar logs para errores de codificación

### Problema: Excel no abre correctamente
- Verificar que Apache POI esté instalado
- Usar versión 5.2.5 o superior
- Verificar formato de números y fechas

### Problema: Queries lentas
- Verificar índices en tablas
- Analizar planes de ejecución con EXPLAIN
- Considerar materializar vistas para reportes frecuentes

### Problema: Datos inconsistentes
- Verificar zona horaria (usar UTC consistentemente)
- Verificar que las fechas incluyan todo el día (23:59:59)
- Verificar estados de reservas incluidos en cálculos
