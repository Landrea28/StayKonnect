# Guía de Optimización y Performance - StayKonnect

## 📊 Métricas Objetivo

- **Tiempo de respuesta API**: < 200ms (p95)
- **Throughput**: > 1000 req/s
- **Tiempo de carga página**: < 2s
- **Queries DB**: < 100ms
- **Cache hit rate**: > 80%

## 🚀 Optimizaciones Implementadas

### 1. Sistema de Caché (Caffeine)

#### Configuración
```properties
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m
```

#### Cachés Configurados
- **propiedades**: 30 min TTL, 500 entradas
- **usuarios**: 10 min TTL, 1000 entradas
- **busqueda**: 5 min TTL, 2000 entradas
- **estadisticas**: 15 min TTL, 200 entradas

#### Uso en Servicios
```java
@Cacheable(value = "propiedades", key = "#id")
public PropiedadDTO obtenerPropiedad(Long id) { ... }

@CacheEvict(value = "propiedades", key = "#id")
public PropiedadDTO actualizarPropiedad(Long id) { ... }

@CacheEvict(value = "propiedades", allEntries = true)
public void limpiarCache() { ... }
```

#### Monitoreo de Caché
```java
// Ver estadísticas
CacheStats stats = caffeine.stats();
log.info("Hit rate: {}, Miss rate: {}", 
    stats.hitRate(), stats.missRate());
```

### 2. Pool de Conexiones HikariCP

#### Configuración Óptima
```properties
# Tamaño del pool
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20

# Timeouts
spring.datasource.hikari.idle-timeout=300000      # 5 min
spring.datasource.hikari.max-lifetime=1800000     # 30 min
spring.datasource.hikari.connection-timeout=20000 # 20 seg

# Detección de leaks
spring.datasource.hikari.leak-detection-threshold=60000 # 60 seg
```

#### Fórmula para Calcular Pool Size
```
connections = ((core_count * 2) + effective_spindle_count)
```

Para servidor con 4 cores + SSD (spindle=1):
```
connections = (4 * 2) + 1 = 9
```

### 3. Optimización de Queries JPA

#### Batch Processing
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

#### Evitar N+1 Queries
```java
// ❌ Mal - causa N+1
@OneToMany(fetch = FetchType.LAZY)
private List<Reserva> reservas;

// ✅ Bien - usa @EntityGraph
@EntityGraph(attributePaths = {"reservas", "valoraciones"})
Propiedad findById(Long id);

// ✅ Bien - usa JOIN FETCH
@Query("SELECT p FROM Propiedad p JOIN FETCH p.reservas WHERE p.id = :id")
Propiedad findByIdWithReservas(@Param("id") Long id);
```

#### DTOs en lugar de Entidades
```java
// ❌ Mal - carga toda la entidad
public Propiedad listar() {
    return propiedadRepository.findAll();
}

// ✅ Bien - solo datos necesarios
public List<PropiedadResumenDTO> listar() {
    return propiedadRepository.findAll().stream()
        .map(this::convertirAResumen)
        .collect(Collectors.toList());
}
```

### 4. Índices de Base de Datos

#### Índices Creados
```sql
-- Búsquedas frecuentes
CREATE INDEX idx_propiedad_ciudad_visible 
ON propiedad(ciudad, visible) WHERE visible = true;

-- Queries con rango
CREATE INDEX idx_propiedad_precio_noche ON propiedad(precio_noche);

-- Joins frecuentes
CREATE INDEX idx_reserva_propiedad_estado ON reserva(propiedad_id, estado);

-- Búsquedas compuestas
CREATE INDEX idx_propiedad_busqueda_completa 
ON propiedad(ciudad, tipo_propiedad, precio_noche, visible);
```

#### Verificar Uso de Índices
```sql
EXPLAIN ANALYZE
SELECT * FROM propiedad 
WHERE ciudad = 'Bogotá' AND visible = true;
```

### 5. Compresión HTTP

#### Configuración
```properties
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,application/json
server.compression.min-response-size=1024
```

#### Reducción Esperada
- JSON: 70-80%
- HTML: 60-70%
- Imágenes ya comprimidas: 0-5%

### 6. Paginación y Limitación

#### Siempre Usar Paginación
```java
// ❌ Mal - puede devolver miles de registros
public List<Propiedad> listar() {
    return propiedadRepository.findAll();
}

// ✅ Bien - paginado
public Page<PropiedadDTO> listar(Pageable pageable) {
    return propiedadRepository.findAll(pageable)
        .map(this::convertirADTO);
}
```

#### Tamaños Recomendados
- **Listados normales**: 20 items
- **Búsquedas**: 10-15 items
- **Feeds infinitos**: 20-30 items
- **Máximo absoluto**: 100 items

### 7. Lazy Loading

#### Configuración Global
```properties
spring.jpa.open-in-view=false
```

#### Fetch Selectivo
```java
@Entity
public class Propiedad {
    @OneToMany(fetch = FetchType.LAZY) // Default
    private List<Reserva> reservas;
    
    @ManyToOne(fetch = FetchType.LAZY) // Cargar solo cuando necesario
    private Usuario anfitrion;
}
```

### 8. Async Processing

#### Métodos Asíncronos
```java
@Async
public CompletableFuture<void> enviarNotificaciones() {
    // Procesamiento en background
    return CompletableFuture.completedFuture(null);
}
```

#### Configuración Thread Pool
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        return executor;
    }
}
```

## 📈 Monitoreo de Performance

### 1. Spring Boot Actuator

#### Endpoints Habilitados
```properties
management.endpoints.web.exposure.include=health,info,metrics,httptrace
```

#### Métricas Disponibles
- `/actuator/metrics/jvm.memory.used`
- `/actuator/metrics/http.server.requests`
- `/actuator/metrics/hikaricp.connections.active`
- `/actuator/metrics/cache.gets`

### 2. Logging de Queries Lentas

```java
@Aspect
@Component
public class PerformanceMonitoringAspect {
    @Around("execution(* com.staykonnect.service.*.*(..))")
    public Object monitor(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        
        if (time > 1000) {
            log.warn("SLOW: {} tomó {} ms", 
                joinPoint.getSignature(), time);
        }
        return result;
    }
}
```

### 3. Estadísticas de Hibernate

```properties
# Solo en desarrollo
spring.jpa.properties.hibernate.generate_statistics=true
```

```java
Statistics stats = entityManager.unwrap(Session.class)
    .getSessionFactory().getStatistics();
log.info("Queries: {}, Time: {} ms", 
    stats.getQueryExecutionCount(),
    stats.getQueryExecutionMaxTime());
```

## 🔧 Mejores Prácticas

### 1. Queries

✅ **DO**
- Usar proyecciones para SELECT específicos
- Implementar paginación siempre
- Usar índices en columnas de búsqueda
- Cachear resultados estables
- Usar batch processing para inserts/updates

❌ **DON'T**
- SELECT * en queries
- Cargar entidades completas cuando solo necesitas campos específicos
- Queries sin límite
- N+1 queries
- Transacciones largas

### 2. Caché

✅ **DO**
- Cachear datos que no cambian frecuentemente
- Usar TTL apropiado según volatilidad
- Implementar cache warming
- Monitorear hit rate

❌ **DON'T**
- Cachear datos sensibles sin encriptar
- TTL muy largo para datos volátiles
- Cachear todo indiscriminadamente

### 3. API Design

✅ **DO**
- Usar DTOs en lugar de entidades
- Implementar filtrado en servidor
- Comprimir respuestas grandes
- Usar ETags para caching HTTP

❌ **DON'T**
- Exponer entidades JPA directamente
- Devolver arrays grandes sin paginación
- Incluir datos innecesarios en respuestas

## 🧪 Testing de Performance

### Load Testing con Apache Bench
```bash
ab -n 1000 -c 10 http://localhost:8080/api/propiedades
```

### JMeter Test Plan
```xml
<ThreadGroup>
  <numThreads>100</numThreads>
  <rampTime>10</rampTime>
  <duration>60</duration>
</ThreadGroup>
```

### Benchmarks Esperados
- Búsqueda de propiedades: < 150ms
- Obtener detalle: < 50ms (con caché)
- Crear reserva: < 300ms
- Listar mensajes: < 100ms

## 📚 Recursos

- [HikariCP Best Practices](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [Hibernate Performance Tips](https://vladmihalcea.com/tutorials/hibernate/)
- [Spring Cache Documentation](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [PostgreSQL Index Types](https://www.postgresql.org/docs/current/indexes-types.html)
