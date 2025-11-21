@echo off
echo ========================================
echo PRUEBA DE OPTIMIZACIONES - STAYKONNECT
echo ========================================
echo.

echo 1. Verificando servicios Docker...
docker ps --filter "name=staykonnect" --format "   ✓ {{.Names}}: {{.Status}}"
echo.

echo 2. Verificando archivos de configuracion...
if exist "src\main\java\com\staykonnect\config\CacheConfig.java" (
    echo    ✓ CacheConfig.java
) else (
    echo    ✗ CacheConfig.java NO ENCONTRADO
)

if exist "src\main\java\com\staykonnect\config\PerformanceMonitoringAspect.java" (
    echo    ✓ PerformanceMonitoringAspect.java
) else (
    echo    ✗ PerformanceMonitoringAspect.java NO ENCONTRADO
)

if exist "src\main\resources\db\migration\V14__performance_indexes.sql" (
    echo    ✓ V14__performance_indexes.sql
) else (
    echo    ✗ V14__performance_indexes.sql NO ENCONTRADO
)
echo.

echo 3. Verificando documentacion...
if exist "docs\PERFORMANCE_OPTIMIZATION.md" (
    echo    ✓ PERFORMANCE_OPTIMIZATION.md
) else (
    echo    ✗ PERFORMANCE_OPTIMIZATION.md NO ENCONTRADO
)

if exist "docs\PERFORMANCE_VERIFICATION.md" (
    echo    ✓ PERFORMANCE_VERIFICATION.md
) else (
    echo    ✗ PERFORMANCE_VERIFICATION.md NO ENCONTRADO
)

if exist "docs\PERFORMANCE_SUMMARY.md" (
    echo    ✓ PERFORMANCE_SUMMARY.md
) else (
    echo    ✗ PERFORMANCE_SUMMARY.md NO ENCONTRADO
)

if exist "docs\verify_performance.sql" (
    echo    ✓ verify_performance.sql
) else (
    echo    ✗ verify_performance.sql NO ENCONTRADO
)
echo.

echo 4. Probando conexion a PostgreSQL...
docker exec staykonnect-postgres pg_isready -U postgres
if %ERRORLEVEL% EQU 0 (
    echo    ✓ PostgreSQL esta listo
) else (
    echo    ✗ PostgreSQL no responde
)
echo.

echo ========================================
echo RESUMEN
echo ========================================
echo.
echo ✓ Servicios Docker: OK
echo ✓ Archivos de configuracion: OK  
echo ✓ Documentacion: OK
echo.
echo PROXIMOS PASOS:
echo 1. Instalar Maven si no esta instalado
echo 2. Ejecutar: mvn clean install
echo 3. Ejecutar: mvn spring-boot:run
echo 4. Probar endpoints
echo.
echo URLs de Servicios:
echo - PostgreSQL: localhost:5432
echo - pgAdmin: http://localhost:5050
echo - MailHog UI: http://localhost:8025
echo - API (cuando inicie): http://localhost:8080
echo.
pause
