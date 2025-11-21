-- ==============================================
-- Script de Verificación de Performance
-- StayKonnect Database Optimization Validation
-- ==============================================

-- 1. VERIFICAR ÍNDICES CREADOS
-- ==============================================
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes 
WHERE indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- Resultado esperado: 20+ índices con nombres que empiezan con 'idx_'

-- 2. TAMAÑO DE ÍNDICES
-- ==============================================
SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE indexrelname LIKE 'idx_%'
ORDER BY pg_relation_size(indexrelid) DESC;

-- 3. VERIFICAR USO DE ÍNDICES EN QUERIES FRECUENTES
-- ==============================================

-- Query 1: Búsqueda de propiedades por ciudad
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM propiedad 
WHERE ciudad = 'Bogotá' AND visible = true
LIMIT 20;
-- Debería usar: idx_propiedad_ciudad_visible

-- Query 2: Buscar reservas por viajero y estado
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM reserva 
WHERE viajero_id = 1 AND estado = 'CONFIRMADA'
ORDER BY fecha_checkin DESC;
-- Debería usar: idx_reserva_viajero_estado

-- Query 3: Pagos por estado y fecha
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM pago 
WHERE estado = 'COMPLETADO' 
ORDER BY created_date DESC
LIMIT 50;
-- Debería usar: idx_pago_estado_fecha

-- Query 4: Mensajes no leídos
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM mensaje 
WHERE destinatario_id = 1 AND leido = false
ORDER BY created_date DESC;
-- Debería usar: idx_mensaje_leido

-- Query 5: Valoraciones de una propiedad
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT * FROM valoracion 
WHERE propiedad_id = 1 AND visible = true
ORDER BY created_date DESC;
-- Debería usar: idx_valoracion_propiedad_visible

-- 4. ESTADÍSTICAS DE USO DE ÍNDICES
-- ==============================================
SELECT 
    schemaname,
    tablename,
    indexrelname as index_name,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;

-- Los índices con idx_scan > 0 están siendo utilizados

-- 5. VERIFICAR ESTADÍSTICAS DE TABLAS
-- ==============================================
SELECT 
    schemaname,
    relname as table_name,
    n_tup_ins as inserts,
    n_tup_upd as updates,
    n_tup_del as deletes,
    n_live_tup as live_rows,
    n_dead_tup as dead_rows,
    last_vacuum,
    last_autovacuum,
    last_analyze,
    last_autoanalyze
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;

-- 6. IDENTIFICAR TABLAS SIN ÍNDICES ADECUADOS
-- ==============================================
SELECT 
    schemaname,
    tablename,
    seq_scan as sequential_scans,
    seq_tup_read as rows_read_sequentially,
    idx_scan as index_scans,
    CASE 
        WHEN seq_scan = 0 THEN 0
        ELSE ROUND(100.0 * idx_scan / (seq_scan + idx_scan), 2)
    END as index_usage_percentage
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY seq_scan DESC;

-- Tablas con bajo index_usage_percentage necesitan más índices

-- 7. QUERIES MÁS LENTAS (requiere pg_stat_statements)
-- ==============================================
-- Habilitar extensión primero si no está activa:
-- CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

SELECT 
    LEFT(query, 100) as query_preview,
    calls,
    ROUND(total_exec_time::numeric, 2) as total_time_ms,
    ROUND(mean_exec_time::numeric, 2) as avg_time_ms,
    ROUND(max_exec_time::numeric, 2) as max_time_ms,
    ROUND((100 * total_exec_time / sum(total_exec_time) OVER ())::numeric, 2) as percent_total_time
FROM pg_stat_statements
WHERE query NOT LIKE '%pg_%'
ORDER BY mean_exec_time DESC
LIMIT 20;

-- 8. CACHE HIT RATE DE POSTGRESQL
-- ==============================================
SELECT 
    'cache hit rate' as metric,
    ROUND(
        100.0 * sum(heap_blks_hit) / NULLIF(sum(heap_blks_hit) + sum(heap_blks_read), 0),
        2
    ) as percentage
FROM pg_statio_user_tables;

-- Ideal: >95% (significa que PostgreSQL está usando bien su cache)

-- 9. TAMAÑO DE TABLAS Y SUS ÍNDICES
-- ==============================================
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) as table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) - pg_relation_size(schemaname||'.'||tablename)) as indexes_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- 10. CONEXIONES ACTIVAS (HikariCP)
-- ==============================================
SELECT 
    datname as database,
    usename as username,
    application_name,
    client_addr,
    state,
    COUNT(*) as connection_count
FROM pg_stat_activity
WHERE datname = current_database()
GROUP BY datname, usename, application_name, client_addr, state
ORDER BY connection_count DESC;

-- Debería haber entre 5-20 conexiones del pool de HikariCP

-- 11. VERIFICAR LOCKS Y BLOQUEOS
-- ==============================================
SELECT 
    pg_class.relname as table_name,
    pg_locks.mode,
    pg_locks.granted,
    pg_stat_activity.query,
    pg_stat_activity.state,
    pg_stat_activity.query_start
FROM pg_locks
JOIN pg_class ON pg_locks.relation = pg_class.oid
JOIN pg_stat_activity ON pg_locks.pid = pg_stat_activity.pid
WHERE pg_class.relkind = 'r'
ORDER BY pg_stat_activity.query_start;

-- No debería haber muchos locks no granted

-- 12. ANÁLISIS DE FRAGMENTACIÓN
-- ==============================================
SELECT 
    schemaname,
    tablename,
    ROUND(100 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_tuple_percent,
    n_dead_tup,
    n_live_tup,
    last_vacuum,
    last_autovacuum
FROM pg_stat_user_tables
WHERE n_dead_tup > 0
ORDER BY dead_tuple_percent DESC;

-- Si dead_tuple_percent > 10%, considerar VACUUM ANALYZE manual

-- 13. RECOMENDACIONES DE ÍNDICES FALTANTES
-- ==============================================
-- Basado en sequential scans frecuentes
SELECT 
    schemaname,
    tablename,
    seq_scan,
    seq_tup_read,
    CASE 
        WHEN seq_scan > 0 THEN seq_tup_read / seq_scan
        ELSE 0
    END as avg_rows_per_scan
FROM pg_stat_user_tables
WHERE seq_scan > 100
  AND seq_tup_read / NULLIF(seq_scan, 0) > 1000
ORDER BY seq_scan DESC;

-- Tablas que aparecen aquí podrían beneficiarse de índices adicionales

-- 14. BENCHMARK DE PERFORMANCE
-- ==============================================
-- Ejecutar queries de prueba y medir tiempo

-- Inicio
\timing on

-- Test 1: Join completo (sin cache)
SELECT 
    p.titulo,
    u.nombre,
    COUNT(r.id) as total_reservas,
    AVG(v.puntuacion) as puntuacion_promedio
FROM propiedad p
JOIN usuario u ON p.anfitrion_id = u.id
LEFT JOIN reserva r ON r.propiedad_id = p.id
LEFT JOIN valoracion v ON v.propiedad_id = p.id
WHERE p.visible = true
GROUP BY p.id, u.id
ORDER BY total_reservas DESC
LIMIT 10;
-- Tiempo esperado: <100ms con índices

-- Test 2: Búsqueda compleja
SELECT *
FROM propiedad
WHERE ciudad IN ('Bogotá', 'Medellín', 'Cali')
  AND tipo_propiedad = 'APARTAMENTO'
  AND precio_noche BETWEEN 50000 AND 200000
  AND visible = true
  AND puntuacion_promedio >= 4.0
ORDER BY puntuacion_promedio DESC, precio_noche ASC
LIMIT 20;
-- Tiempo esperado: <50ms con idx_propiedad_busqueda_completa

-- Test 3: Agregación pesada
SELECT 
    DATE_TRUNC('month', r.created_date) as mes,
    COUNT(*) as total_reservas,
    SUM(r.precio_total) as ingresos_totales,
    AVG(r.precio_total) as ticket_promedio,
    COUNT(DISTINCT r.viajero_id) as viajeros_unicos
FROM reserva r
WHERE r.estado IN ('CONFIRMADA', 'COMPLETADA')
  AND r.created_date >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY mes
ORDER BY mes DESC;
-- Tiempo esperado: <200ms

\timing off

-- 15. COMANDO DE MANTENIMIENTO
-- ==============================================
-- Ejecutar periódicamente para mantener performance

-- Actualizar estadísticas (importante después de crear índices)
ANALYZE propiedad;
ANALYZE reserva;
ANALYZE pago;
ANALYZE mensaje;
ANALYZE valoracion;
ANALYZE notificaciones;
ANALYZE usuario;

-- Limpiar dead tuples
VACUUM ANALYZE propiedad;
VACUUM ANALYZE reserva;

-- Reindexar si es necesario (solo en mantenimiento programado)
-- REINDEX TABLE propiedad;
-- REINDEX TABLE reserva;

-- ==============================================
-- FIN DEL SCRIPT DE VERIFICACIÓN
-- ==============================================

-- RESUMEN DE CHECKS:
-- ✓ Índices creados correctamente
-- ✓ Índices siendo utilizados en queries
-- ✓ Cache hit rate >95%
-- ✓ Conexiones del pool controladas (5-20)
-- ✓ No hay locks bloqueantes
-- ✓ Fragmentación baja (<10% dead tuples)
-- ✓ Queries críticas <100ms
-- ✓ Estadísticas actualizadas
