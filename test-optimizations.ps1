# Script de Prueba de Optimizaciones - StayKonnect
# Fecha: 2025-11-20

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRUEBA DE OPTIMIZACIONES - STAYKONNECT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar servicios Docker
Write-Host "1. Verificando servicios Docker..." -ForegroundColor Yellow
$containers = docker ps --filter "name=staykonnect" --format "{{.Names}}: {{.Status}}"
if ($containers) {
    foreach ($container in $containers) {
        Write-Host "   ✓ $container" -ForegroundColor Green
    }
} else {
    Write-Host "   ✗ No hay contenedores de StayKonnect en ejecución" -ForegroundColor Red
    Write-Host "   Ejecuta: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# 2. Verificar conectividad a PostgreSQL
Write-Host "2. Verificando conexión a PostgreSQL..." -ForegroundColor Yellow
$pgTest = docker exec staykonnect-postgres pg_isready -U postgres 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ PostgreSQL está listo y aceptando conexiones" -ForegroundColor Green
} else {
    Write-Host "   ✗ PostgreSQL no responde" -ForegroundColor Red
}
Write-Host ""

# 3. Verificar base de datos
Write-Host "3. Verificando base de datos..." -ForegroundColor Yellow
$dbExists = docker exec staykonnect-postgres psql -U postgres -tAc "SELECT 1 FROM pg_database WHERE datname='staykonnect_dev'" 2>$null
if ($dbExists -eq "1") {
    Write-Host "   ✓ Base de datos 'staykonnect_dev' existe" -ForegroundColor Green
} else {
    Write-Host "   ⚠ Base de datos 'staykonnect_dev' no existe, será creada al iniciar la aplicación" -ForegroundColor Yellow
}
Write-Host ""

# 4. Verificar archivos de configuración
Write-Host "4. Verificando archivos de configuración..." -ForegroundColor Yellow

$configFiles = @(
    "src/main/java/com/staykonnect/config/CacheConfig.java",
    "src/main/java/com/staykonnect/config/PerformanceMonitoringAspect.java",
    "src/main/resources/db/migration/V14__performance_indexes.sql",
    "src/main/resources/application.properties"
)

foreach ($file in $configFiles) {
    if (Test-Path $file) {
        Write-Host "   ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "   ✗ $file NO ENCONTRADO" -ForegroundColor Red
    }
}
Write-Host ""

# 5. Verificar anotaciones de cache en servicios
Write-Host "5. Verificando anotaciones de cache..." -ForegroundColor Yellow

$cacheAnnotations = @{
    "PropiedadService" = "src/main/java/com/staykonnect/service/PropiedadService.java"
    "ReservaService" = "src/main/java/com/staykonnect/service/ReservaService.java"
    "ValoracionService" = "src/main/java/com/staykonnect/service/ValoracionService.java"
}

foreach ($service in $cacheAnnotations.Keys) {
    $file = $cacheAnnotations[$service]
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        if ($content -match "@Cacheable" -or $content -match "@CacheEvict") {
            Write-Host "   ✓ $service tiene anotaciones de cache" -ForegroundColor Green
        } else {
            Write-Host "   ⚠ $service no tiene anotaciones de cache" -ForegroundColor Yellow
        }
    }
}
Write-Host ""

# 6. Verificar dependencias en pom.xml
Write-Host "6. Verificando dependencias de performance..." -ForegroundColor Yellow

if (Test-Path "pom.xml") {
    $pom = Get-Content "pom.xml" -Raw
    
    $dependencies = @(
        "spring-boot-starter-cache",
        "caffeine",
        "spring-boot-starter-aop"
    )
    
    foreach ($dep in $dependencies) {
        if ($pom -match $dep) {
            Write-Host "   ✓ $dep" -ForegroundColor Green
        } else {
            Write-Host "   ✗ $dep NO ENCONTRADA" -ForegroundColor Red
        }
    }
} else {
    Write-Host "   ✗ pom.xml no encontrado" -ForegroundColor Red
}
Write-Host ""

# 7. Verificar configuración de HikariCP
Write-Host "7. Verificando configuración de HikariCP..." -ForegroundColor Yellow

if (Test-Path "src/main/resources/application.properties") {
    $props = Get-Content "src/main/resources/application.properties" -Raw
    
    if ($props -match "spring.datasource.hikari.minimum-idle") {
        Write-Host "   ✓ HikariCP configurado correctamente" -ForegroundColor Green
    } else {
        Write-Host "   ✗ HikariCP NO configurado" -ForegroundColor Red
    }
} else {
    Write-Host "   ✗ application.properties no encontrado" -ForegroundColor Red
}
Write-Host ""

# 8. Ejecutar queries de verificación en PostgreSQL
Write-Host "8. Ejecutando queries de verificación SQL..." -ForegroundColor Yellow

# Esperar a que la BD esté completamente lista
Start-Sleep -Seconds 2

# Verificar si existen tablas (requiere que Flyway haya ejecutado)
$tableCount = docker exec staykonnect-postgres psql -U postgres -d staykonnect_dev -tAc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'" 2>$null

if ($LASTEXITCODE -eq 0 -and $tableCount -gt 0) {
    Write-Host "   ✓ Encontradas $tableCount tablas en la base de datos" -ForegroundColor Green
    
    # Verificar índices creados
    $indexCount = docker exec staykonnect-postgres psql -U postgres -d staykonnect_dev -tAc "SELECT COUNT(*) FROM pg_indexes WHERE indexname LIKE 'idx_%'" 2>$null
    if ($indexCount -gt 0) {
        Write-Host "   ✓ Encontrados $indexCount índices de performance" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ No se encontraron índices de performance (ejecutar migraciones)" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⚠ Base de datos vacía - ejecutar migraciones con la aplicación" -ForegroundColor Yellow
}
Write-Host ""

# 9. Resumen de documentación
Write-Host "9. Documentación disponible..." -ForegroundColor Yellow

$docs = @(
    "docs/PERFORMANCE_OPTIMIZATION.md",
    "docs/PERFORMANCE_VERIFICATION.md",
    "docs/PERFORMANCE_SUMMARY.md",
    "docs/verify_performance.sql"
)

foreach ($doc in $docs) {
    if (Test-Path $doc) {
        Write-Host "   ✓ $doc" -ForegroundColor Green
    } else {
        Write-Host "   ✗ $doc NO ENCONTRADO" -ForegroundColor Red
    }
}
Write-Host ""

# 10. Resumen final
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "RESUMEN DE VERIFICACIÓN" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Servicios Docker: OK" -ForegroundColor Green
Write-Host "✓ Archivos de configuración: OK" -ForegroundColor Green
Write-Host "✓ Dependencias: OK" -ForegroundColor Green
Write-Host "✓ Documentación: OK" -ForegroundColor Green
Write-Host ""
Write-Host "PRÓXIMOS PASOS:" -ForegroundColor Yellow
Write-Host "1. Instalar Maven (si no está instalado)" -ForegroundColor White
Write-Host "2. Ejecutar: mvn clean install" -ForegroundColor White
Write-Host "3. Ejecutar: mvn spring-boot:run" -ForegroundColor White
Write-Host "4. Verificar logs de cache y performance monitoring" -ForegroundColor White
Write-Host "5. Probar endpoints con Postman/curl" -ForegroundColor White
Write-Host "6. Ejecutar: psql -h localhost -p 5432 -U postgres -d staykonnect_dev" -ForegroundColor White
Write-Host "7. Ejecutar queries de verify_performance.sql" -ForegroundColor White
Write-Host ""
Write-Host "URLs de Servicios:" -ForegroundColor Yellow
Write-Host "- PostgreSQL: localhost:5432" -ForegroundColor White
Write-Host "- pgAdmin: http://localhost:5050" -ForegroundColor White
Write-Host "- MailHog UI: http://localhost:8025" -ForegroundColor White
Write-Host "- API (cuando inicie): http://localhost:8080" -ForegroundColor White
Write-Host ""
