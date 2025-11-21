# Guía de Instalación de Maven - Windows

## 📦 Método 1: Instalación Automática con Script (RECOMENDADO)

### Paso 1: Ejecutar PowerShell como Administrador
1. Presiona `Windows + X`
2. Selecciona **"Windows PowerShell (Admin)"** o **"Terminal (Admin)"**
3. Confirma en el diálogo de permisos

### Paso 2: Ejecutar el Script
```powershell
cd c:\Users\dulce\OneDrive\Documentos\Gh\demo
powershell -ExecutionPolicy Bypass -File .\install-maven.ps1
```

### Paso 3: Reiniciar PowerShell
1. Cierra todas las ventanas de PowerShell
2. Abre una nueva ventana de PowerShell
3. Verifica la instalación:
```powershell
mvn --version
```

---

## 🔧 Método 2: Instalación Manual

### Paso 1: Descargar Maven
1. Ir a: https://maven.apache.org/download.cgi
2. Descargar: `apache-maven-3.9.6-bin.zip`
3. Guardar en: `C:\Users\dulce\Downloads`

### Paso 2: Extraer el Archivo
1. Clic derecho en el archivo ZIP
2. Seleccionar **"Extraer todo..."**
3. Extraer a: `C:\Program Files\Apache\Maven`
4. Resultado: `C:\Program Files\Apache\Maven\apache-maven-3.9.6`

### Paso 3: Configurar Variables de Entorno

#### A. Agregar MAVEN_HOME
1. Presiona `Windows + Pause` o `Windows + X` → **Sistema**
2. Clic en **"Configuración avanzada del sistema"**
3. Clic en **"Variables de entorno"**
4. En **"Variables del sistema"**, clic en **"Nueva..."**
5. Configurar:
   - **Nombre:** `MAVEN_HOME`
   - **Valor:** `C:\Program Files\Apache\Maven\apache-maven-3.9.6`
6. Clic en **"Aceptar"**

#### B. Agregar Maven al PATH
1. En **"Variables del sistema"**, selecciona **"Path"**
2. Clic en **"Editar..."**
3. Clic en **"Nuevo"**
4. Agregar: `%MAVEN_HOME%\bin`
5. Clic en **"Aceptar"** en todos los diálogos

### Paso 4: Verificar Java (Requisito)
```powershell
java -version
```
**Debe mostrar:** Java 21 o superior

### Paso 5: Verificar Maven
1. **CERRAR todas las ventanas de PowerShell**
2. Abrir una **NUEVA** ventana de PowerShell
3. Ejecutar:
```powershell
mvn --version
```

**Salida esperada:**
```
Apache Maven 3.9.6
Maven home: C:\Program Files\Apache\Maven\apache-maven-3.9.6
Java version: 23.0.2
```

---

## 🚀 Método 3: Con Chocolatey (Alternativa)

Si tienes Chocolatey instalado:

```powershell
# Como Administrador
choco install maven
```

Si NO tienes Chocolatey, instalarlo primero:
```powershell
# Como Administrador
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

---

## ✅ Verificación de Instalación

### 1. Verificar Maven
```powershell
mvn --version
```

### 2. Verificar Variables de Entorno
```powershell
echo $env:MAVEN_HOME
echo $env:Path | Select-String maven
```

### 3. Probar Compilación
```powershell
cd c:\Users\dulce\OneDrive\Documentos\Gh\demo
mvn clean validate
```

---

## 🐛 Solución de Problemas

### Problema 1: "mvn no se reconoce como comando"
**Causa:** Variables de entorno no actualizadas

**Solución:**
1. Cerrar TODAS las ventanas de PowerShell/CMD
2. Abrir una NUEVA ventana
3. Si persiste, reiniciar la computadora

### Problema 2: "JAVA_HOME no está configurado"
**Solución:**
```powershell
# Configurar JAVA_HOME manualmente
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-23", [System.EnvironmentVariableTarget]::Machine)
```

### Problema 3: Error de permisos
**Solución:**
- Ejecutar PowerShell como Administrador
- O instalar en una carpeta con permisos: `C:\Maven`

### Problema 4: Maven descarga muy lento
**Solución:**
```powershell
# Usar mirror más rápido - crear settings.xml
New-Item -Path "$env:USERPROFILE\.m2" -ItemType Directory -Force
```

Crear archivo `$env:USERPROFILE\.m2\settings.xml`:
```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
</settings>
```

---

## 📋 Checklist Post-Instalación

- [ ] Maven instalado en `C:\Program Files\Apache\Maven`
- [ ] Variable `MAVEN_HOME` configurada
- [ ] Maven agregado al `PATH`
- [ ] PowerShell reiniciado
- [ ] `mvn --version` muestra versión correcta
- [ ] `mvn clean validate` funciona en el proyecto
- [ ] Listo para compilar StayKonnect

---

## 🎯 Próximos Pasos Después de Instalar Maven

### 1. Compilar el Proyecto
```powershell
cd c:\Users\dulce\OneDrive\Documentos\Gh\demo
mvn clean install -DskipTests
```

### 2. Ejecutar la Aplicación
```powershell
mvn spring-boot:run
```

### 3. Verificar Logs
Buscar en la salida:
- ✅ "Creating new Caffeine cache" - Cache inicializado
- ✅ "HikariPool-1 - Start completed" - Pool de conexiones listo
- ✅ "Flyway successfully applied 14 migration(s)" - Índices creados
- ✅ "Started StayKonnectApplication" - Aplicación corriendo

### 4. Probar Endpoints
```powershell
# En otra terminal
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/propiedades
```

---

## 📞 Ayuda Adicional

- **Documentación oficial:** https://maven.apache.org/install.html
- **Guía de inicio:** https://maven.apache.org/guides/getting-started/
- **Troubleshooting:** https://maven.apache.org/troubleshooting.html

---

**Fecha:** 2025-11-20  
**Versión Maven:** 3.9.6  
**Java Requerido:** 21 o superior (tienes 23.0.2 ✓)  
**Sistema:** Windows 11
