# Resumen Paso 12: Panel Avanzado (Reportes y Análisis)

## 📊 Descripción General

Panel avanzado de reportes y análisis para administradores con capacidades de exportación PDF/Excel, generación de gráficos y análisis profundo de ingresos, ocupación, comisiones y estacionalidad.

## 🎯 Objetivos Cumplidos

1. ✅ Reportes de ingresos con análisis temporal y tasas de crecimiento
2. ✅ Reportes de ocupación por propiedad con métricas detalladas
3. ✅ Análisis de comisiones por período
4. ✅ Rankings de top propiedades y anfitriones
5. ✅ Análisis de estacionalidad para identificar temporadas
6. ✅ Exportación a PDF profesional con tablas formateadas
7. ✅ Exportación a Excel con estilos y fórmulas
8. ✅ Generación de datos para gráficos (Chart.js compatible)
9. ✅ Queries nativas optimizadas con PostgreSQL
10. ✅ Seguridad: Solo acceso ADMIN

## 📈 Estadísticas

- **Archivos creados**: 11
  - 8 DTOs (ReporteIngresoDTO, ReporteOcupacionDTO, ReporteComisionDTO, TopPropiedadDTO, TopAnfitrionDTO, EstacionalidadDTO, DatosGraficoDTO, ExportRequest)
  - 1 Servicio (ReporteService - 600+ líneas)
  - 1 Controlador (ReporteController - 11 endpoints)
  - 1 Documentación de testing

- **Archivos modificados**: 2
  - pom.xml (dependencias Apache POI + iText7)
  - ReservaRepository.java (10 queries nativas nuevas)
  - README.md (actualización Fase 12)

- **Endpoints REST**: 11
  - 6 endpoints de consulta de reportes
  - 3 endpoints de datos para gráficos
  - 2 endpoints de exportación (PDF/Excel)

- **Queries nativas SQL**: 10
  - obtenerIngresosPorPeriodo
  - obtenerOcupacionPorPropiedad
  - obtenerTopPropiedadesPorIngresos
  - obtenerTopAnfitriones
  - obtenerEstacionalidadPorMes
  - obtenerComisionesPorPeriodo
  - calcularTasaOcupacionGlobal
  - Y más...

## 🏗️ Arquitectura

### DTOs de Reportes
```
dto/reporte/
├── ReporteIngresoDTO.java      - Ingresos por período (9 campos)
├── ReporteOcupacionDTO.java    - Ocupación por propiedad (12 campos)
├── ReporteComisionDTO.java     - Comisiones por período (9 campos)
├── TopPropiedadDTO.java        - Ranking propiedades (13 campos)
├── TopAnfitrionDTO.java        - Ranking anfitriones (12 campos)
├── EstacionalidadDTO.java      - Análisis mensual (9 campos)
├── DatosGraficoDTO.java        - Datos para Chart.js (4 campos + SerieGraficoDTO)
└── ExportRequest.java          - Parámetros de exportación (7 campos)
```

### Servicio de Reportes
```java
ReporteService (600+ líneas)
├── Generación de reportes
│   ├── generarReporteIngresos()       - Por período con tasa crecimiento
│   ├── generarReporteOcupacion()      - Por propiedad con métricas
│   ├── generarReporteComisiones()     - Por período con promedios
│   ├── obtenerTopPropiedades()        - Top N por ingresos
│   ├── obtenerTopAnfitriones()        - Top N por desempeño
│   └── analizarEstacionalidad()       - 12 meses con temporadas
│
├── Generación de gráficos
│   ├── generarGraficoIngresos()       - Line chart multi-serie
│   ├── generarGraficoOcupacion()      - Bar chart top 10
│   └── generarGraficoEstacionalidad() - Mixed chart (line + bar)
│
├── Exportación
│   ├── exportarIngresosPDF()          - iText7 con tablas
│   └── exportarIngresosExcel()        - Apache POI con estilos
│
└── Utilidades
    ├── mapearPeriodo()                - MENSUAL -> 'month'
    └── formatearPeriodo()             - Fecha -> "octubre 2024"
```

### Controlador REST
```java
ReporteController (11 endpoints)
├── GET /api/reportes/ingresos              - Reporte ingresos
├── GET /api/reportes/ocupacion             - Reporte ocupación
├── GET /api/reportes/comisiones            - Reporte comisiones
├── GET /api/reportes/top-propiedades       - Top propiedades
├── GET /api/reportes/top-anfitriones       - Top anfitriones
├── GET /api/reportes/estacionalidad        - Análisis estacionalidad
├── GET /api/reportes/grafico/ingresos      - Datos gráfico ingresos
├── GET /api/reportes/grafico/ocupacion     - Datos gráfico ocupación
├── GET /api/reportes/grafico/estacionalidad - Datos gráfico estacionalidad
├── GET /api/reportes/exportar/ingresos/pdf - Descarga PDF
└── GET /api/reportes/exportar/ingresos/excel - Descarga Excel
```

## 🔑 Características Principales

### 1. Análisis de Ingresos
- **Períodos flexibles**: Diario, semanal, mensual, trimestral, anual
- **Métricas calculadas**:
  - Ingresos brutos (suma precio_total)
  - Comisiones (suma comision)
  - Ingresos netos (brutos - comisiones)
  - Número de reservas por período
  - Ingreso promedio por reserva
  - **Tasa de crecimiento** vs período anterior (%)
- **Agrupación**: `DATE_TRUNC` para PostgreSQL
- **Estados incluidos**: CONFIRMADA, PAGADA, EN_CURSO, COMPLETADA

### 2. Análisis de Ocupación
- **Métricas por propiedad**:
  - Días disponibles (calculado del período)
  - Días reservados (suma de días de reservas)
  - Días bloqueados (futuro: integración con sistema de bloqueos)
  - **Tasa de ocupación** ((días reservados / días disponibles) * 100)
  - Número de reservas
  - Ingresos generados
  - **Ingreso por día reservado** (optimización de precios)
  - Puntuación promedio de valoraciones
- **Solo propiedades visibles**
- **LEFT JOIN** para incluir propiedades sin reservas

### 3. Análisis de Comisiones
- **Métricas**:
  - Comisiones generadas (suma comision)
  - Comisiones reales (completadas)
  - Comisiones pendientes (en proceso)
  - Número de transacciones
  - Comisión promedio
  - **Porcentaje de comisión promedio** ((comisiones / ingresos) * 100)
  - Ingresos totales base
- **Agrupación por período**: Igual que ingresos

### 4. Top Propiedades
- **Criterios de ranking**: Ingresos generados (descendente)
- **Métricas incluidas**:
  - ID y nombre de propiedad
  - Ciudad, tipo
  - ID y nombre de anfitrión
  - Ingresos generados
  - Número de reservas
  - **Tasa de ocupación** (calculada)
  - Puntuación promedio
  - Número de valoraciones
  - Precio promedio por noche
  - **Posición en ranking** (1 a N)
- **Límite configurable**: Default 10, máximo personalizable

### 5. Top Anfitriones
- **Criterios de ranking**: Ingresos generados (descendente)
- **Métricas incluidas**:
  - ID, nombre, email
  - Número total de propiedades
  - Propiedades activas (visibles)
  - Número total de reservas
  - Reservas completadas
  - **Tasa de completamiento** ((completadas / totales) * 100)
  - Ingresos generados
  - Puntuación promedio de valoraciones
  - Número de valoraciones recibidas
  - **Posición en ranking**
- **Límite configurable**: Default 10

### 6. Análisis de Estacionalidad
- **Granularidad**: Mensual (12 meses)
- **Métricas por mes**:
  - Número del mes (1-12)
  - Nombre del mes (español)
  - **Temporada** (ALTA, MEDIA, BAJA)
    - ALTA: ≥ 120% de promedio
    - BAJA: ≤ 80% de promedio
    - MEDIA: Entre 80% y 120%
  - Número de reservas
  - Ingresos generados
  - Tasa de ocupación promedio
  - Precio promedio por noche
  - Número de viajeros únicos
  - Tasa de crecimiento anual (vs año anterior - futuro)
- **Query por año**: Filtra por EXTRACT(YEAR FROM fecha_checkin)

### 7. Exportación PDF (iText7)
- **Formato profesional**:
  - Título centrado en negrita (20pt)
  - Subtítulo con período (12pt)
  - Tabla con headers en gris
  - 6 columnas: Período, Reservas, Ingresos Brutos, Comisiones, Ingresos Netos, Promedio/Reserva
  - Fila de totales en negrita
  - Auto-width columns
- **Formato monetario**: $#,##0.00 (COP)
- **Content-Type**: application/pdf
- **Descarga**: Content-Disposition attachment

### 8. Exportación Excel (Apache POI)
- **Formato profesional**:
  - Hoja "Reporte de Ingresos"
  - Fila de título con estilo header
  - Fila de período
  - Headers con fondo gris y negrita
  - 7 columnas con datos completos
  - Formato de moneda para campos de dinero
  - Fila de totales con estilo header
  - Auto-size de columnas
- **Content-Type**: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
- **Extensión**: .xlsx (Office 2007+)

### 9. Datos para Gráficos
- **Formato Chart.js compatible**:
  - `titulo`: Título del gráfico
  - `tipo`: line, bar, pie, area
  - `etiquetas`: Array de strings (eje X)
  - `series`: Array de SerieGraficoDTO
    - `nombre`: Nombre de la serie
    - `color`: Color hex (#rrggbb)
    - `datos`: Array de BigDecimal
    - `tipo`: Para gráficos mixtos (opcional)

- **3 gráficos implementados**:
  1. **Gráfico de Ingresos** (line chart)
     - 3 series: Ingresos Brutos, Comisiones, Ingresos Netos
     - Colores: #10b981 (verde), #f59e0b (naranja), #3b82f6 (azul)
  
  2. **Gráfico de Ocupación** (bar chart)
     - Top 10 propiedades por tasa de ocupación
     - 1 serie: Tasa de Ocupación (%)
     - Color: #8b5cf6 (púrpura)
     - Nombres truncados a 20 caracteres
  
  3. **Gráfico de Estacionalidad** (mixed chart)
     - 2 series: Número de Reservas (bar) + Ingresos en miles (line)
     - Colores: #ec4899 (rosa), #06b6d4 (cyan)
     - 12 meses en eje X

## 🔍 Queries Nativas Optimizadas

### 1. Ingresos por Período
```sql
SELECT DATE_TRUNC(:periodo, r.created_date) as fecha,
       COUNT(*) as numero_reservas,
       SUM(r.precio_total) as ingresos_brutos,
       SUM(r.comision) as comisiones,
       SUM(r.precio_total - r.comision) as ingresos_netos
FROM reserva r
WHERE r.created_date BETWEEN :fechaInicio AND :fechaFin
  AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')
GROUP BY DATE_TRUNC(:periodo, r.created_date)
ORDER BY fecha
```

**Optimizaciones**:
- Índice en `created_date` (existente)
- Índice en `estado` (existente)
- `DATE_TRUNC` eficiente para agrupación

### 2. Ocupación por Propiedad
```sql
SELECT p.id, p.nombre, p.ciudad, p.tipo,
       COUNT(r.id) as numero_reservas,
       SUM(r.precio_total) as ingresos,
       COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio,
       SUM(DATE_PART('day', r.fecha_checkout - r.fecha_checkin)) as dias_reservados
FROM propiedad p
LEFT JOIN reserva r ON p.id = r.propiedad_id
    AND r.created_date BETWEEN :fechaInicio AND :fechaFin
    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')
LEFT JOIN valoracion v ON p.id = v.propiedad_id
WHERE p.visible = true
GROUP BY p.id, p.nombre, p.ciudad, p.tipo
ORDER BY ingresos DESC
```

**Optimizaciones**:
- LEFT JOIN para incluir propiedades sin reservas
- COALESCE para manejar NULL en promedios
- DATE_PART para cálculo de días
- Índices en FK (propiedad_id, reserva.created_date)

### 3. Top Propiedades
```sql
SELECT p.id, p.nombre, p.ciudad, p.tipo,
       u.id as anfitrion_id, 
       CONCAT(u.nombre, ' ', u.apellido) as anfitrion_nombre,
       COUNT(r.id) as numero_reservas,
       SUM(r.precio_total) as ingresos,
       COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio,
       COUNT(DISTINCT v.id) as numero_valoraciones,
       AVG(r.precio_total / NULLIF(DATE_PART('day', r.fecha_checkout - r.fecha_checkin), 0)) 
           as precio_promedio_noche
FROM propiedad p
INNER JOIN usuario u ON p.anfitrion_id = u.id
LEFT JOIN reserva r ON p.id = r.propiedad_id
    AND r.created_date BETWEEN :fechaInicio AND :fechaFin
    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')
LEFT JOIN valoracion v ON p.id = v.propiedad_id
WHERE p.visible = true
GROUP BY p.id, p.nombre, p.ciudad, p.tipo, u.id, u.nombre, u.apellido
ORDER BY ingresos DESC
LIMIT :limite
```

**Optimizaciones**:
- INNER JOIN con usuario (siempre existe anfitrion_id)
- NULLIF para evitar división por cero
- COUNT DISTINCT para valoraciones
- LIMIT para restringir resultados

### 4. Top Anfitriones
```sql
SELECT u.id, 
       CONCAT(u.nombre, ' ', u.apellido) as nombre, 
       u.email,
       COUNT(DISTINCT p.id) as numero_propiedades,
       COUNT(DISTINCT CASE WHEN p.visible = true THEN p.id END) as propiedades_activas,
       COUNT(r.id) as numero_reservas,
       COUNT(CASE WHEN r.estado = 'COMPLETADA' THEN 1 END) as reservas_completadas,
       SUM(r.precio_total) as ingresos,
       COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio,
       COUNT(DISTINCT v.id) as numero_valoraciones
FROM usuario u
INNER JOIN propiedad p ON u.id = p.anfitrion_id
LEFT JOIN reserva r ON p.id = r.propiedad_id
    AND r.created_date BETWEEN :fechaInicio AND :fechaFin
    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')
LEFT JOIN valoracion v ON u.id = v.valorado_id
WHERE u.rol = 'ANFITRION'
GROUP BY u.id, u.nombre, u.apellido, u.email
ORDER BY ingresos DESC
LIMIT :limite
```

**Optimizaciones**:
- COUNT DISTINCT para propiedades (un anfitrión puede tener múltiples)
- CASE WHEN para contar propiedades activas
- COUNT CASE para contar completadas
- Filtro por rol ANFITRION

### 5. Estacionalidad por Mes
```sql
SELECT EXTRACT(MONTH FROM r.fecha_checkin) as mes,
       COUNT(r.id) as numero_reservas,
       SUM(r.precio_total) as ingresos,
       AVG(r.precio_total / NULLIF(DATE_PART('day', r.fecha_checkout - r.fecha_checkin), 0)) 
           as precio_promedio_noche,
       COUNT(DISTINCT r.viajero_id) as numero_viajeros
FROM reserva r
WHERE EXTRACT(YEAR FROM r.fecha_checkin) = :anio
  AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')
GROUP BY EXTRACT(MONTH FROM r.fecha_checkin)
ORDER BY mes
```

**Optimizaciones**:
- EXTRACT para extraer mes y año
- COUNT DISTINCT para viajeros únicos
- Índice en fecha_checkin

## 🔐 Seguridad

### Autenticación y Autorización
```java
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {
    // Todos los endpoints requieren ADMIN
}
```

- ✅ Todos los endpoints requieren autenticación JWT
- ✅ Solo usuarios con rol ADMIN pueden acceder
- ✅ No se exponen datos sensibles de usuarios (emails solo en tops)
- ✅ Validación de parámetros de entrada

### Validaciones
- Fechas: `fechaInicio` < `fechaFin`
- Período: DIARIO, SEMANAL, MENSUAL, TRIMESTRAL, ANUAL
- Límite: 1-100 (razonable para tops)
- Año: Validación implícita por PostgreSQL

## 📊 Métricas de Performance

### Tiempos Esperados
| Operación | Tiempo Esperado | Optimización |
|-----------|----------------|--------------|
| Reporte ingresos | < 3s | DATE_TRUNC + índices |
| Reporte ocupación | < 5s | LEFT JOIN optimizado |
| Top propiedades | < 2s | LIMIT + índices |
| Top anfitriones | < 2s | LIMIT + índices |
| Estacionalidad | < 2s | EXTRACT + índices |
| Exportar PDF | < 5s | iText7 optimizado |
| Exportar Excel | < 8s | Apache POI streaming |

### Índices Utilizados
- `reserva.created_date` (B-tree)
- `reserva.estado` (B-tree)
- `reserva.propiedad_id` (FK index)
- `reserva.viajero_id` (FK index)
- `propiedad.anfitrion_id` (FK index)
- `valoracion.propiedad_id` (FK index)
- `valoracion.valorado_id` (FK index)

## 🧪 Testing

### Endpoints de Consulta
```bash
# Ingresos mensuales Q4 2024
GET /api/reportes/ingresos
  ?fechaInicio=2024-10-01T00:00:00
  &fechaFin=2024-12-31T23:59:59
  &periodo=MENSUAL

# Ocupación noviembre 2024
GET /api/reportes/ocupacion
  ?fechaInicio=2024-11-01T00:00:00
  &fechaFin=2024-11-30T23:59:59

# Top 10 propiedades
GET /api/reportes/top-propiedades
  ?fechaInicio=2024-10-01T00:00:00
  &fechaFin=2024-12-31T23:59:59
  &limite=10

# Estacionalidad 2024
GET /api/reportes/estacionalidad?anio=2024
```

### Endpoints de Gráficos
```bash
# Gráfico de ingresos
GET /api/reportes/grafico/ingresos
  ?fechaInicio=2024-10-01T00:00:00
  &fechaFin=2024-12-31T23:59:59
  &periodo=MENSUAL

# Gráfico de ocupación
GET /api/reportes/grafico/ocupacion
  ?fechaInicio=2024-11-01T00:00:00
  &fechaFin=2024-11-30T23:59:59

# Gráfico de estacionalidad
GET /api/reportes/grafico/estacionalidad?anio=2024
```

### Endpoints de Exportación
```bash
# Descargar PDF
GET /api/reportes/exportar/ingresos/pdf
  ?fechaInicio=2024-10-01T00:00:00
  &fechaFin=2024-12-31T23:59:59
  &periodo=MENSUAL
# Descarga: reporte-ingresos.pdf

# Descargar Excel
GET /api/reportes/exportar/ingresos/excel
  ?fechaInicio=2024-10-01T00:00:00
  &fechaFin=2024-12-31T23:59:59
  &periodo=MENSUAL
# Descarga: reporte-ingresos.xlsx
```

### Casos de Prueba
1. ✅ Reporte con datos vacíos (sin reservas en período)
2. ✅ Reporte con un solo registro
3. ✅ Reporte con múltiples períodos
4. ✅ Cálculo correcto de tasas de crecimiento
5. ✅ Cálculo correcto de tasa de ocupación
6. ✅ Rankings correctos (orden descendente)
7. ✅ Clasificación de temporadas (alta/media/baja)
8. ✅ PDF con formato correcto
9. ✅ Excel con celdas formateadas
10. ✅ Datos de gráficos con estructura correcta

## 🚀 Próximos Pasos

### Mejoras Futuras
1. **Reportes adicionales**:
   - Reporte de cancelaciones con análisis de razones
   - Reporte de disputas y resoluciones
   - Reporte de cumplimiento normativo (GDPR)
   - Reporte de crecimiento de usuarios por canal

2. **Exportación avanzada**:
   - Exportar todos los reportes (no solo ingresos)
   - Plantillas personalizables de PDF
   - Excel con gráficos embebidos
   - CSV para análisis en herramientas externas

3. **Análisis predictivo**:
   - Proyecciones de ingresos basadas en tendencias
   - Predicción de temporadas altas/bajas
   - Alertas de anomalías (caídas significativas)
   - Recomendaciones de precios dinámicos

4. **Dashboards interactivos**:
   - Filtros en tiempo real
   - Drill-down en métricas
   - Comparaciones año vs año
   - Segmentación por ciudad/tipo de propiedad

5. **Optimizaciones**:
   - Cache de reportes frecuentes (Redis)
   - Materialización de vistas para queries pesadas
   - Jobs programados para pre-calcular métricas
   - Compresión de archivos grandes

6. **Integración**:
   - Email automático de reportes semanales/mensuales
   - Webhooks para alertas de métricas
   - API para herramientas de BI (Tableau, Power BI)
   - Exportación a Google Sheets

## 📝 Notas Técnicas

### Dependencias Agregadas
```xml
<!-- Apache POI for Excel -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>

<!-- iText7 for PDF -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>
```

### Configuración Recomendada
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          fetch_size: 50  # Para queries grandes
          batch_size: 25
  
  task:
    execution:
      pool:
        core-size: 5
        max-size: 10
        queue-capacity: 100

# Timeout para reportes pesados
spring:
  mvc:
    async:
      request-timeout: 30000  # 30 segundos
```

### Consideraciones de Escalabilidad
- Para más de 100,000 reservas: Considerar paginación en reportes
- Para exportaciones grandes: Implementar generación asíncrona con notificación
- Para queries muy lentas: Crear vistas materializadas en PostgreSQL
- Para alto volumen: Implementar cache de reportes con TTL

## ✅ Checklist de Implementación

- [x] Agregar dependencias (Apache POI, iText7)
- [x] Crear 8 DTOs de reportes
- [x] Agregar 10 queries nativas en ReservaRepository
- [x] Implementar ReporteService con 15+ métodos
- [x] Implementar ReporteController con 11 endpoints
- [x] Generar gráficos compatibles con Chart.js
- [x] Exportar a PDF con iText7
- [x] Exportar a Excel con Apache POI
- [x] Documentar testing completo
- [x] Actualizar README
- [x] Verificar seguridad (ADMIN only)
- [x] Verificar compilación (0 errores)

## 🎉 Conclusión

El Paso 12 está **completamente implementado** con un sistema robusto de reportes avanzados que permite a los administradores:

1. Analizar ingresos con métricas profundas y tendencias
2. Evaluar ocupación de propiedades para optimización
3. Monitorear comisiones y rentabilidad
4. Identificar mejores propiedades y anfitriones
5. Planificar estrategias basadas en estacionalidad
6. Exportar reportes profesionales en PDF y Excel
7. Visualizar datos en gráficos interactivos

**Estado**: ✅ Completado (12/20 pasos = 60% del proyecto)

**Siguiente paso**: RF10 - Sistema de Notificaciones
