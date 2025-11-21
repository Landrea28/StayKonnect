# Script para Instalar Maven en Windows
# Fecha: 2025-11-20

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "INSTALACION DE APACHE MAVEN" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Verificar permisos de administrador
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "Este script necesita permisos de administrador" -ForegroundColor Yellow
    Write-Host "Por favor, ejecuta PowerShell como Administrador y vuelve a ejecutar este script" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Clic derecho en PowerShell -> Ejecutar como administrador" -ForegroundColor White
    pause
    exit 1
}

Write-Host "Ejecutando con permisos de administrador" -ForegroundColor Green
Write-Host ""

# Configuracion
$mavenVersion = "3.9.6"
$mavenUrl = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$downloadPath = "$env:TEMP\apache-maven-$mavenVersion-bin.zip"
$installPath = "C:\Program Files\Apache\Maven"
$extractPath = "$installPath\apache-maven-$mavenVersion"

Write-Host "Configuracion:" -ForegroundColor Yellow
Write-Host "   Version: Maven $mavenVersion" -ForegroundColor White
Write-Host "   Destino: $installPath" -ForegroundColor White
Write-Host ""

# Paso 1: Descargar Maven
Write-Host "Paso 1: Descargando Maven..." -ForegroundColor Yellow
try {
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath -UseBasicParsing
    Write-Host "   Descarga completada" -ForegroundColor Green
} catch {
    Write-Host "   Error al descargar Maven" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
    pause
    exit 1
}
Write-Host ""

# Paso 2: Crear directorio de instalacion
Write-Host "Paso 2: Creando directorio de instalacion..." -ForegroundColor Yellow
if (-not (Test-Path $installPath)) {
    New-Item -ItemType Directory -Path $installPath -Force | Out-Null
    Write-Host "   Directorio creado: $installPath" -ForegroundColor Green
} else {
    Write-Host "   Directorio ya existe" -ForegroundColor Green
}
Write-Host ""

# Paso 3: Extraer Maven
Write-Host "Paso 3: Extrayendo archivos..." -ForegroundColor Yellow
try {
    if (Test-Path $extractPath) {
        Remove-Item -Path $extractPath -Recurse -Force
        Write-Host "   Instalacion anterior eliminada" -ForegroundColor Yellow
    }
    
    Expand-Archive -Path $downloadPath -DestinationPath $installPath -Force
    Write-Host "   Archivos extraidos" -ForegroundColor Green
} catch {
    Write-Host "   Error al extraer" -ForegroundColor Red
    Write-Host "   $_" -ForegroundColor Red
    pause
    exit 1
}
Write-Host ""

# Paso 4: Configurar variables de entorno
Write-Host "Paso 4: Configurando variables de entorno..." -ForegroundColor Yellow

[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $extractPath, [System.EnvironmentVariableTarget]::Machine)
Write-Host "   MAVEN_HOME configurado" -ForegroundColor Green

$currentPath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
$mavenBinPath = "$extractPath\bin"

if ($currentPath -notlike "*$mavenBinPath*") {
    $newPath = $currentPath + ";" + $mavenBinPath
    [System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)
    Write-Host "   Maven agregado al PATH" -ForegroundColor Green
} else {
    Write-Host "   Maven ya esta en el PATH" -ForegroundColor Green
}
Write-Host ""

# Paso 5: Limpiar archivos temporales
Write-Host "Paso 5: Limpiando archivos temporales..." -ForegroundColor Yellow
Remove-Item -Path $downloadPath -Force -ErrorAction SilentlyContinue
Write-Host "   Archivos temporales eliminados" -ForegroundColor Green
Write-Host ""

# Resumen
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "INSTALACION COMPLETADA" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "IMPORTANTE:" -ForegroundColor Yellow
Write-Host "1. CIERRA esta ventana de PowerShell" -ForegroundColor White
Write-Host "2. Abre una NUEVA ventana de PowerShell" -ForegroundColor White
Write-Host "3. Ejecuta: mvn --version" -ForegroundColor White
Write-Host "4. Deberias ver: Apache Maven $mavenVersion" -ForegroundColor White
Write-Host ""
Write-Host "Ubicacion de Maven:" -ForegroundColor Yellow
Write-Host "   $extractPath" -ForegroundColor White
Write-Host ""
Write-Host "Proximos pasos:" -ForegroundColor Yellow
Write-Host "   cd c:\Users\dulce\OneDrive\Documentos\Gh\demo" -ForegroundColor White
Write-Host "   mvn clean install" -ForegroundColor White
Write-Host "   mvn spring-boot:run" -ForegroundColor White
Write-Host ""
pause
