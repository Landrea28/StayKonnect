@echo off
echo ============================================
echo INSTALACION DE APACHE MAVEN
echo ============================================
echo.

echo Descargando Maven 3.9.6...
powershell -Command "Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'"
echo Descarga completada
echo.

echo Creando directorio de instalacion...
if not exist "C:\Program Files\Apache\Maven" mkdir "C:\Program Files\Apache\Maven"
echo.

echo Extrayendo archivos...
powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath 'C:\Program Files\Apache\Maven' -Force"
echo Archivos extraidos
echo.

echo Configurando variables de entorno...
setx MAVEN_HOME "C:\Program Files\Apache\Maven\apache-maven-3.9.6" /M
setx PATH "%PATH%;C:\Program Files\Apache\Maven\apache-maven-3.9.6\bin" /M
echo Variables configuradas
echo.

echo Limpiando archivos temporales...
del "%TEMP%\maven.zip"
echo.

echo ============================================
echo INSTALACION COMPLETADA
echo ============================================
echo.
echo IMPORTANTE:
echo 1. CIERRA esta ventana
echo 2. Abre una NUEVA ventana de CMD o PowerShell
echo 3. Ejecuta: mvn --version
echo 4. Deberias ver: Apache Maven 3.9.6
echo.
echo Proximos pasos:
echo    cd c:\Users\dulce\OneDrive\Documentos\Gh\demo
echo    mvn clean install
echo    mvn spring-boot:run
echo.
pause
