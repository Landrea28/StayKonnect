# Guía de Pruebas - Sistema de Autenticación

Este documento proporciona instrucciones para probar el sistema de autenticación implementado en el Paso 4.

## Configuración Previa

### 1. Iniciar los servicios

```powershell
# Iniciar PostgreSQL, pgAdmin y MailHog
docker-compose up -d

# Verificar que los servicios están corriendo
docker-compose ps
```

### 2. Iniciar la aplicación

```bash
mvn spring-boot:run
```

### 3. Verificar que la aplicación está corriendo

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **MailHog** (emails de prueba): http://localhost:8025

## Casos de Prueba

### 1. Registro de Usuario

**Endpoint**: `POST /api/auth/register`

**Request Body**:
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@example.com",
  "password": "Password123!",
  "confirmarPassword": "Password123!",
  "telefono": "+573001234567",
  "fechaNacimiento": "1990-05-15",
  "rol": "VIAJERO",
  "biografia": "Amante de los viajes y nuevas experiencias"
}
```

**Respuesta Esperada** (201 Created):
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente. Por favor, verifica tu email.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 6,
    "email": "juan.perez@example.com",
    "nombreCompleto": "Juan Pérez",
    "rol": "VIAJERO",
    "emailVerificado": false
  }
}
```

**Verificar**:
- El usuario se crea en la base de datos con estado `PENDIENTE_VERIFICACION`
- Se envía un email de verificación a MailHog (http://localhost:8025)
- Se devuelve un token JWT válido

### 2. Verificación de Email

**Endpoint**: `GET /api/auth/verify-email?token={token}`

**Pasos**:
1. Ir a MailHog: http://localhost:8025
2. Abrir el email de verificación
3. Copiar el token desde el enlace de verificación
4. Hacer GET request con el token

**Respuesta Esperada** (200 OK):
```json
{
  "success": true,
  "message": "Email verificado exitosamente. Tu cuenta está activa.",
  "data": null
}
```

**Verificar**:
- El usuario cambia a estado `ACTIVA`
- El campo `emailVerificado` cambia a `true`
- El token de verificación se elimina

### 3. Login de Usuario

**Endpoint**: `POST /api/auth/login`

**Request Body**:
```json
{
  "email": "juan.perez@example.com",
  "password": "Password123!"
}
```

**Respuesta Esperada** (200 OK):
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "userId": 6,
    "email": "juan.perez@example.com",
    "nombreCompleto": "Juan Pérez",
    "rol": "VIAJERO",
    "emailVerificado": true
  }
}
```

### 4. Solicitar Recuperación de Contraseña

**Endpoint**: `POST /api/auth/recover-password`

**Request Body**:
```json
{
  "email": "juan.perez@example.com"
}
```

**Respuesta Esperada** (200 OK):
```json
{
  "success": true,
  "message": "Si el email está registrado, recibirás un correo con las instrucciones.",
  "data": null
}
```

**Verificar**:
- Se envía un email a MailHog con el token de recuperación
- El token se guarda en el campo `tokenRecuperacion` del usuario

### 5. Resetear Contraseña

**Endpoint**: `POST /api/auth/reset-password`

**Pasos**:
1. Obtener el token de recuperación desde el email en MailHog
2. Hacer el request con el token y la nueva contraseña

**Request Body**:
```json
{
  "token": "token-desde-email",
  "nuevaPassword": "NewPassword456!",
  "confirmarPassword": "NewPassword456!"
}
```

**Respuesta Esperada** (200 OK):
```json
{
  "success": true,
  "message": "Contraseña actualizada exitosamente. Ya puedes iniciar sesión.",
  "data": null
}
```

**Verificar**:
- La contraseña se actualiza (hash BCrypt)
- El token de recuperación se elimina
- Se puede hacer login con la nueva contraseña

### 6. Acceso a Endpoints Protegidos

Para probar el sistema de autorización basado en roles:

**Request con JWT**:
```http
GET /api/usuarios/perfil
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Verificar roles**:
- Endpoints públicos: accesibles sin token
- Endpoints de anfitrión: solo con rol `ANFITRION`
- Endpoints de admin: solo con rol `ADMIN`

## Pruebas de Validación

### 1. Contraseña Débil

**Request**:
```json
{
  "email": "test@example.com",
  "password": "123456",
  "...": "..."
}
```

**Respuesta Esperada** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial"
}
```

### 2. Email Duplicado

**Request**: Intentar registrar un email ya existente

**Respuesta Esperada** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El email ya está registrado"
}
```

### 3. Contraseñas No Coinciden

**Request**:
```json
{
  "password": "Password123!",
  "confirmarPassword": "DifferentPassword123!"
}
```

**Respuesta Esperada** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Las contraseñas no coinciden"
}
```

### 4. Credenciales Inválidas

**Request**: Login con contraseña incorrecta

**Respuesta Esperada** (401 Unauthorized):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas o token expirado. Por favor, inicie sesión nuevamente."
}
```

### 5. Cuenta Bloqueada

Si intentas hacer login con una cuenta bloqueada:

**Respuesta Esperada** (400 Bad Request):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "La cuenta está bloqueada. Contacte al soporte."
}
```

## Datos de Prueba Precargados

Puedes usar estos usuarios que ya están en la base de datos (Flyway V10):

### Administrador
```json
{
  "email": "admin@staykonnect.com",
  "password": "Admin123!",
  "rol": "ADMIN"
}
```

### Anfitrión 1
```json
{
  "email": "carlos.host@example.com",
  "password": "Admin123!",
  "rol": "ANFITRION"
}
```

### Anfitrión 2
```json
{
  "email": "maria.host@example.com",
  "password": "Admin123!",
  "rol": "ANFITRION"
}
```

### Viajero 1
```json
{
  "email": "juan.traveler@example.com",
  "password": "Admin123!",
  "rol": "VIAJERO"
}
```

### Viajero 2
```json
{
  "email": "ana.traveler@example.com",
  "password": "Admin123!",
  "rol": "VIAJERO"
}
```

## Pruebas con Swagger

1. Ir a: http://localhost:8080/swagger-ui.html
2. Buscar "Autenticación" en la lista de controladores
3. Expandir los endpoints y hacer "Try it out"
4. Para endpoints protegidos:
   - Hacer login primero
   - Copiar el `accessToken` de la respuesta
   - Hacer clic en "Authorize" (candado verde)
   - Pegar: `Bearer {accessToken}`
   - Ahora puedes probar endpoints protegidos

## Pruebas con cURL

### Registro
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Pedro",
    "apellido": "García",
    "email": "pedro.garcia@example.com",
    "password": "Password123!",
    "confirmarPassword": "Password123!",
    "telefono": "+573009876543",
    "fechaNacimiento": "1995-03-20",
    "rol": "ANFITRION"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@staykonnect.com",
    "password": "Admin123!"
  }'
```

### Endpoint Protegido
```bash
curl -X GET http://localhost:8080/api/usuarios/perfil \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## Solución de Problemas

### Error: "Unable to connect to database"
- Verificar que PostgreSQL esté corriendo: `docker-compose ps`
- Revisar credenciales en `application-dev.properties`

### Error: "JWT signature does not match"
- El JWT secret cambió, hacer logout y volver a hacer login
- Verificar que `jwt.secret` sea el mismo entre reinicios

### Emails no llegan a MailHog
- Verificar que MailHog esté corriendo: http://localhost:8025
- Revisar configuración SMTP en `application-dev.properties`

## Próximos Pasos

Con el sistema de autenticación funcionando, el siguiente paso es:
- **Paso 5**: Implementar RF02 - Gestión de Propiedades (CRUD)
