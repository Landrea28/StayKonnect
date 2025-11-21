# Script de utilidades para gestión de base de datos
# PowerShell script para Windows

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "StayKonnect - Database Manager" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

function Show-Menu {
    Write-Host "Seleccione una opción:" -ForegroundColor Yellow
    Write-Host "1. Iniciar PostgreSQL (Docker)"
    Write-Host "2. Detener PostgreSQL (Docker)"
    Write-Host "3. Ver estado de contenedores"
    Write-Host "4. Conectar a PostgreSQL (psql)"
    Write-Host "5. Abrir pgAdmin (navegador)"
    Write-Host "6. Ejecutar migraciones Flyway"
    Write-Host "7. Limpiar y recrear base de datos"
    Write-Host "8. Ver logs de PostgreSQL"
    Write-Host "9. Crear backup de base de datos"
    Write-Host "10. Restaurar backup"
    Write-Host "0. Salir"
    Write-Host ""
}

function Start-Database {
    Write-Host "Iniciando PostgreSQL con Docker Compose..." -ForegroundColor Green
    docker-compose up -d postgres pgadmin mailhog
    Start-Sleep -Seconds 5
    Write-Host "PostgreSQL iniciado en puerto 5432" -ForegroundColor Green
    Write-Host "pgAdmin disponible en http://localhost:5050" -ForegroundColor Green
    Write-Host "MailHog disponible en http://localhost:8025" -ForegroundColor Green
}

function Stop-Database {
    Write-Host "Deteniendo PostgreSQL..." -ForegroundColor Yellow
    docker-compose down
    Write-Host "PostgreSQL detenido" -ForegroundColor Green
}

function Show-Status {
    Write-Host "Estado de contenedores:" -ForegroundColor Cyan
    docker-compose ps
}

function Connect-Database {
    Write-Host "Conectando a PostgreSQL..." -ForegroundColor Green
    docker exec -it staykonnect-postgres psql -U postgres -d staykonnect_dev
}

function Open-PgAdmin {
    Write-Host "Abriendo pgAdmin en el navegador..." -ForegroundColor Green
    Start-Process "http://localhost:5050"
}

function Run-Migrations {
    Write-Host "Ejecutando migraciones de Flyway..." -ForegroundColor Green
    mvn flyway:migrate
    Write-Host "Migraciones completadas" -ForegroundColor Green
}

function Reset-Database {
    Write-Host "ADVERTENCIA: Esto eliminará TODOS los datos!" -ForegroundColor Red
    $confirm = Read-Host "¿Está seguro? (s/n)"
    if ($confirm -eq 's' -or $confirm -eq 'S') {
        Write-Host "Limpiando base de datos..." -ForegroundColor Yellow
        mvn flyway:clean
        Write-Host "Ejecutando migraciones..." -ForegroundColor Yellow
        mvn flyway:migrate
        Write-Host "Base de datos recreada exitosamente" -ForegroundColor Green
    } else {
        Write-Host "Operación cancelada" -ForegroundColor Yellow
    }
}

function Show-Logs {
    Write-Host "Mostrando logs de PostgreSQL (Ctrl+C para salir)..." -ForegroundColor Green
    docker logs -f staykonnect-postgres
}

function Create-Backup {
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $backupFile = "backup_$timestamp.sql"
    Write-Host "Creando backup en $backupFile..." -ForegroundColor Green
    docker exec staykonnect-postgres pg_dump -U postgres staykonnect_dev > $backupFile
    Write-Host "Backup creado exitosamente: $backupFile" -ForegroundColor Green
}

function Restore-Backup {
    Write-Host "Archivos de backup disponibles:" -ForegroundColor Cyan
    Get-ChildItem -Filter "backup_*.sql" | ForEach-Object { Write-Host $_.Name }
    Write-Host ""
    $backupFile = Read-Host "Ingrese el nombre del archivo de backup"
    
    if (Test-Path $backupFile) {
        Write-Host "Restaurando backup desde $backupFile..." -ForegroundColor Yellow
        Get-Content $backupFile | docker exec -i staykonnect-postgres psql -U postgres -d staykonnect_dev
        Write-Host "Backup restaurado exitosamente" -ForegroundColor Green
    } else {
        Write-Host "Archivo no encontrado: $backupFile" -ForegroundColor Red
    }
}

# Menú principal
do {
    Show-Menu
    $option = Read-Host "Opción"
    Write-Host ""
    
    switch ($option) {
        '1' { Start-Database }
        '2' { Stop-Database }
        '3' { Show-Status }
        '4' { Connect-Database }
        '5' { Open-PgAdmin }
        '6' { Run-Migrations }
        '7' { Reset-Database }
        '8' { Show-Logs }
        '9' { Create-Backup }
        '10' { Restore-Backup }
        '0' { Write-Host "¡Hasta luego!" -ForegroundColor Cyan; break }
        default { Write-Host "Opción inválida" -ForegroundColor Red }
    }
    
    if ($option -ne '0' -and $option -ne '8') {
        Write-Host ""
        Write-Host "Presione Enter para continuar..." -ForegroundColor Gray
        Read-Host
        Clear-Host
    }
} while ($option -ne '0')
