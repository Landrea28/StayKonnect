# Verificación de Optimizaciones de Performance - StayKonnect

## ✅ Estado de Implementación

### 1. Sistema de Caché ✅
**Ubicación:** `src/main/java/com/staykonnect/config/CacheConfig.java`

**Configuración:**
- ✅ 6 regiones de caché configuradas
- ✅ TTL configurado: 10 minutos write, 5 minutos access
- ✅ Tamaño máximo: 1000 entradas
- ✅ Estadísticas habilitadas con `recordStats()`

**Cachés Configurados:**
```
- propiedades     (30 min TTL, 500 max)
- usuarios        (10 min TTL, 1000 max)
- reservas        (10 min TTL, 1000 max)
- valoraciones    (10 min TTL, 1000 max)
- busqueda        (5 min TTL, 2000 max)
- estadisticas    (15 min TTL, 200 max)
```

### 2. Anotaciones de Cache en Servicios ✅

**PropiedadService:**
- ✅ `obtenerPropiedad(Long id)` → `@Cacheable(value="propiedades", key="#id")`
- ✅ `actualizarPropiedad(Long id, ...)` → `@CacheEvict(value="propiedades", key="#id")`

**ReservaService:**
- ✅ `obtenerReserva(Long id)` → `@Cacheable(value="reservas", key="#id")`
- ✅ `confirmarReserva(Long reservaId)` → `@CacheEvict(value="reservas", key="#reservaId")`
- ✅ `cancelarReserva(Long reservaId, ...)` → `@CacheEvict(value="reservas", key="#reservaId")`

**ValoracionService:**
- ✅ `obtenerEstadisticasPropiedad(Long propiedadId)` → `@Cacheable(value="estadisticas")`
- ✅ `crearValoracion(...)` → `@CacheEvict(value={"estadisticas", "propiedades", "valoraciones"})`

### 3. Índices de Base de Datos ✅
**Ubicación:** `src/main/resources/db/migration/V14__performance_indexes.sql`

**Índices Creados (20+):**
- ✅ Propiedades: ciudad_visible, tipo_visible, precio, puntuación
- ✅ Reservas: viajero_estado, propiedad_estado, fechas, código
- ✅ Pagos: reserva, estado_fecha, stripe_id
- ✅ Mensajes: conversacion (composite), leido (partial)
- ✅ Valoraciones: propiedad_visible (partial), valorado, puntuación
- ✅ Notificaciones: usuario_leida_created, tipo_fecha
- ✅ Joins: propiedad_anfitrion, usuario_rol_activo

### 4. HikariCP Optimización ✅
**Ubicación:** `src/main/resources/application.properties`

**Configuración:**
```properties
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000       # 5 min
spring.datasource.hikari.max-lifetime=1800000      # 30 min
spring.datasource.hikari.connection-timeout=20000  # 20 seg
spring.datasource.hikari.leak-detection-threshold=60000 # 60 seg
```

### 5. JPA Batch Processing ✅
**Ubicación:** `src/main/resources/application.properties`

**Configuración:**
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.batch_versioned_data=true
```

### 6. Compresión HTTP ✅
**Configuración:**
```properties
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,application/json,application/javascript,text/css
server.compression.min-response-size=1024
```

### 7. Performance Monitoring (AOP) ✅
**Ubicación:** `src/main/java/com/staykonnect/config/PerformanceMonitoringAspect.java`

**Funcionalidad:**
- ✅ Monitoreo de servicios (WARNING si >1000ms)
- ✅ Monitoreo de repositorios (WARNING si >500ms)
- ✅ Logging con detalles de timing
- ✅ Captura de errores con timing

---

## 🧪 Plan de Pruebas

### Test 1: Verificar Cache Hit/Miss
```bash
# Iniciar aplicación
# Hacer request a /api/propiedades/1 (primera vez - MISS)
# Hacer request a /api/propiedades/1 (segunda vez - HIT)
# Verificar logs de caché en consola
```

### Test 2: Verificar Índices Aplicados
```sql
-- Conectar a PostgreSQL
psql -h localhost -p 5432 -U staykonnect_user -d staykonnect_db

-- Verificar índices creados
\di idx_*

-- Verificar uso de índices en queries
EXPLAIN ANALYZE
SELECT * FROM propiedad WHERE ciudad = 'Bogotá' AND visible = true;

-- Debería mostrar "Index Scan using idx_propiedad_ciudad_visible"
```

### Test 3: Monitorear Pool de Conexiones
```bash
# Endpoint: GET /actuator/metrics/hikaricp.connections.active
# Verificar número de conexiones activas
# Debería estar entre 5-20 bajo carga
```

### Test 4: Verificar Logging de Performance
```bash
# Buscar en logs:
grep "SLOW" application.log
grep "tomó.*ms" application.log

# Deberías ver warnings para operaciones lentas
```

### Test 5: Verificar Compresión HTTP
```bash
# Request con Accept-Encoding
curl -H "Accept-Encoding: gzip" http://localhost:8080/api/propiedades

# Verificar header Content-Encoding: gzip en response
```

---

## 📊 Métricas Esperadas

### Antes de Optimización (Baseline)
- Tiempo respuesta promedio: ~500ms
- Queries por segundo: ~200
- Conexiones DB simultáneas: Variable (no controlado)
- Cache hit rate: 0% (sin caché)

### Después de Optimización (Target)
- Tiempo respuesta promedio: <200ms (60% mejora)
- Queries por segundo: >1000 (5x mejora)
- Conexiones DB simultáneas: 5-20 (controlado)
- Cache hit rate: >80% (para datos frecuentes)

### KPIs Específicos
- **GET /api/propiedades/{id}**: <50ms (con cache hit)
- **GET /api/propiedades**: <150ms (con índices)
- **POST /api/reservas**: <300ms
- **GET /api/valoraciones/propiedad/{id}**: <100ms

---

## 🔍 Comandos de Verificación

### 1. Ver Estadísticas de Caché (Actuator)
```bash
curl http://localhost:8080/actuator/metrics/cache.gets
curl http://localhost:8080/actuator/metrics/cache.puts
curl http://localhost:8080/actuator/metrics/cache.evictions
```

### 2. Ver Métricas de HikariCP
```bash
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/actuator/metrics/hikaricp.connections.idle
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

### 3. Ver Queries Lentas en PostgreSQL
```sql
-- Habilitar log de queries lentas
ALTER DATABASE staykonnect_db SET log_min_duration_statement = 100;

-- Ver queries lentas en logs
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

### 4. Verificar Tamaño de Cache en Memoria
```bash
# JVM memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Heap memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap
```

---

## 🐛 Troubleshooting

### Problema: Cache no funciona
**Síntomas:** No se ve mejora en tiempos de respuesta
**Solución:**
1. Verificar que `@EnableCaching` está en `CacheConfig`
2. Verificar que las anotaciones `@Cacheable` están presentes
3. Revisar logs: buscar "Creating new Caffeine cache"
4. Verificar que no hay `@Transactional` interfiriendo con el cache

### Problema: Índices no se aplican
**Síntomas:** Queries siguen siendo lentas
**Solución:**
1. Ejecutar migración: `mvn flyway:migrate`
2. Verificar índices: `\di` en psql
3. Ejecutar `ANALYZE` para actualizar estadísticas
4. Usar `EXPLAIN ANALYZE` para verificar plan de ejecución

### Problema: Pool de conexiones agotado
**Síntomas:** `Connection timeout` errors
**Solución:**
1. Aumentar `maximum-pool-size` a 30-50
2. Reducir `connection-timeout` a 10000ms
3. Verificar que no hay conexiones leak
4. Revisar logs de HikariCP leak detection

### Problema: Alto uso de memoria
**Síntomas:** `OutOfMemoryError` o GC frecuente
**Solución:**
1. Reducir tamaño de caché: `maximumSize=500`
2. Reducir TTL: `expireAfterWrite=5m`
3. Aumentar heap de JVM: `-Xmx2g`
4. Revisar memory leaks en cache

---

## 📝 Notas Importantes

1. **Cache Eviction:** Asegurarse de invalidar cache cuando se actualizan entidades relacionadas
2. **Índices Parciales:** Solo funcionan con las condiciones WHERE especificadas
3. **HikariCP:** El tamaño del pool debe ajustarse según carga real (no exceder)
4. **Batch Processing:** Solo efectivo con operaciones batch (>10 items)
5. **Compresión:** Solo efectiva para responses >1KB

---

## ✅ Checklist de Verificación

- [ ] Ejecutar migraciones de BD (V14__performance_indexes.sql)
- [ ] Iniciar aplicación y verificar logs de cache initialization
- [ ] Probar endpoints y verificar tiempos de respuesta
- [ ] Verificar cache hits en Actuator metrics
- [ ] Revisar logs de PerformanceMonitoringAspect
- [ ] Ejecutar EXPLAIN ANALYZE en queries críticas
- [ ] Monitorear HikariCP connections durante carga
- [ ] Verificar compresión HTTP con curl
- [ ] Realizar load testing con JMeter/AB
- [ ] Documentar métricas before/after

---

**Fecha de Implementación:** 2025-11-19  
**Responsable:** Equipo StayKonnect  
**Estado:** ✅ Implementado - En Pruebas
