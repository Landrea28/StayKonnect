# Guía de Pruebas - Gestión de Propiedades (CRUD)

Este documento proporciona instrucciones para probar el sistema de gestión de propiedades implementado en el Paso 5.

## Prerrequisitos

1. Base de datos iniciada: `docker-compose up -d`
2. Aplicación corriendo: `mvn spring-boot:run`
3. Token JWT de un usuario anfitrión (obtener mediante login)

## Obtener Token de Anfitrión

```bash
# Login como anfitrión precargado
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos.host@example.com",
    "password": "Admin123!"
  }'
```

Copiar el `accessToken` de la respuesta para usar en las siguientes peticiones.

---

## 1. Crear una Nueva Propiedad

**Endpoint**: `POST /api/propiedades`  
**Auth**: Bearer Token (solo ANFITRION)

### Request
```bash
curl -X POST http://localhost:8080/api/propiedades \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}" \
  -d '{
    "titulo": "Apartamento moderno en Chapinero",
    "descripcion": "Hermoso apartamento de 2 habitaciones con vista panorámica a la ciudad. Ubicación privilegiada cerca de restaurantes, cafés y transporte público. Ideal para parejas o familias pequeñas. Cuenta con cocina equipada, WiFi de alta velocidad y espacio de trabajo.",
    "tipoPropiedad": "APARTAMENTO",
    "direccion": "Calle 63 #7-45",
    "ciudad": "Bogotá",
    "pais": "Colombia",
    "codigoPostal": "110231",
    "latitud": 4.6533,
    "longitud": -74.0636,
    "habitaciones": 2,
    "camas": 3,
    "banos": 2,
    "capacidad": 4,
    "areaM2": 75.5,
    "precioPorNoche": 150000,
    "precioLimpieza": 50000,
    "depositoSeguridad": 300000,
    "servicios": ["WiFi", "Cocina", "TV", "Aire acondicionado", "Agua caliente"],
    "imagenes": ["/images/apt1-main.jpg", "/images/apt1-room1.jpg", "/images/apt1-kitchen.jpg"],
    "reglasCasa": "No fumar. No mascotas. No fiestas. Respetar horarios de silencio 10pm-7am.",
    "horaCheckin": "15:00:00",
    "horaCheckout": "11:00:00",
    "estanciaMinima": 2,
    "estanciaMaxima": 30
  }'
```

### Respuesta Esperada (201 Created)
```json
{
  "success": true,
  "message": "Propiedad creada exitosamente",
  "data": {
    "id": 5,
    "titulo": "Apartamento moderno en Chapinero",
    "estado": "PENDIENTE_APROBACION",
    "anfitrionId": 2,
    "anfitrionNombre": "Carlos Rodríguez",
    "puntuacionPromedio": 0.0,
    "totalValoraciones": 0,
    ...
  }
}
```

---

## 2. Listar Mis Propiedades

**Endpoint**: `GET /api/propiedades/mis-propiedades`  
**Auth**: Bearer Token (solo ANFITRION)

### Request
```bash
curl -X GET "http://localhost:8080/api/propiedades/mis-propiedades?page=0&size=10&sort=createdDate&direction=DESC" \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}"
```

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Propiedades obtenidas exitosamente",
  "data": {
    "content": [
      {
        "id": 5,
        "titulo": "Apartamento moderno en Chapinero",
        "tipoPropiedad": "APARTAMENTO",
        "ciudad": "Bogotá",
        "pais": "Colombia",
        "precioPorNoche": 150000,
        "estado": "PENDIENTE_APROBACION",
        "imagenPrincipal": "/images/apt1-main.jpg",
        ...
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

---

## 3. Listar Propiedades Activas (Público)

**Endpoint**: `GET /api/propiedades`  
**Auth**: No requerida (público)

### Request
```bash
curl -X GET "http://localhost:8080/api/propiedades?page=0&size=20"
```

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Propiedades obtenidas exitosamente",
  "data": {
    "content": [
      {
        "id": 1,
        "titulo": "Casa de playa en Cartagena",
        "tipoPropiedad": "CASA_COMPLETA",
        "ciudad": "Cartagena",
        "precioPorNoche": 250000,
        "estado": "ACTIVA",
        "puntuacionPromedio": 4.5,
        ...
      }
    ],
    "totalElements": 4,
    "totalPages": 1
  }
}
```

---

## 4. Obtener Detalles de una Propiedad

**Endpoint**: `GET /api/propiedades/{id}`  
**Auth**: No requerida (público)

### Request
```bash
curl -X GET http://localhost:8080/api/propiedades/1
```

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Propiedad encontrada",
  "data": {
    "id": 1,
    "titulo": "Casa de playa en Cartagena",
    "descripcion": "Amplia casa con piscina privada...",
    "tipoPropiedad": "CASA_COMPLETA",
    "direccion": "Sector Manzanillo del Mar",
    "ciudad": "Cartagena",
    "pais": "Colombia",
    "habitaciones": 3,
    "camas": 4,
    "banos": 2,
    "capacidad": 6,
    "precioPorNoche": 250000,
    "servicios": ["Piscina", "WiFi", "Cocina", "BBQ"],
    "imagenes": [...],
    "reglasCasa": "No fumar...",
    "estado": "ACTIVA",
    "anfitrionNombre": "Carlos Rodríguez",
    "createdDate": "2024-11-15T10:30:00",
    ...
  }
}
```

---

## 5. Actualizar una Propiedad

**Endpoint**: `PUT /api/propiedades/{id}`  
**Auth**: Bearer Token (solo propietario o ADMIN)

### Request (actualización parcial)
```bash
curl -X PUT http://localhost:8080/api/propiedades/5 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}" \
  -d '{
    "titulo": "Apartamento moderno y renovado en Chapinero",
    "precioPorNoche": 160000,
    "descripcion": "ACTUALIZADO: Hermoso apartamento recién renovado con muebles nuevos..."
  }'
```

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Propiedad actualizada exitosamente",
  "data": {
    "id": 5,
    "titulo": "Apartamento moderno y renovado en Chapinero",
    "precioPorNoche": 160000,
    "lastModifiedDate": "2024-11-19T14:25:00",
    ...
  }
}
```

---

## 6. Cambiar Estado de una Propiedad

**Endpoint**: `PATCH /api/propiedades/{id}/estado`  
**Auth**: Bearer Token (solo propietario o ADMIN)

### Request
```bash
# Activar propiedad
curl -X PATCH "http://localhost:8080/api/propiedades/5/estado?estado=ACTIVA" \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}"

# Desactivar propiedad
curl -X PATCH "http://localhost:8080/api/propiedades/5/estado?estado=INACTIVA" \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}"
```

### Estados Disponibles
- `ACTIVA` - Visible y disponible para reservas
- `INACTIVA` - No visible en búsquedas
- `PENDIENTE_APROBACION` - Esperando revisión
- `BLOQUEADA` - Bloqueada por admin
- `ELIMINADA` - Eliminada lógicamente

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Estado de propiedad actualizado",
  "data": {
    "id": 5,
    "estado": "ACTIVA",
    ...
  }
}
```

---

## 7. Eliminar una Propiedad

**Endpoint**: `DELETE /api/propiedades/{id}`  
**Auth**: Bearer Token (solo propietario o ADMIN)

### Request
```bash
curl -X DELETE http://localhost:8080/api/propiedades/5 \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}"
```

### Respuesta Esperada (200 OK)
```json
{
  "success": true,
  "message": "Propiedad eliminada exitosamente",
  "data": null
}
```

**Nota**: La eliminación es lógica, la propiedad cambia a estado `ELIMINADA` pero no se borra de la BD.

---

## 8. Subir Imágenes

### Subir una imagen

**Endpoint**: `POST /api/files/upload-image`  
**Auth**: Bearer Token

```bash
curl -X POST http://localhost:8080/api/files/upload-image \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}" \
  -F "file=@/ruta/a/tu/imagen.jpg"
```

**Respuesta**:
```json
{
  "success": true,
  "message": "Imagen subida exitosamente",
  "data": {
    "url": "/images/e7a3f123-45bc-6789-def0-1234567890ab.jpg"
  }
}
```

### Subir múltiples imágenes

**Endpoint**: `POST /api/files/upload-images`

```bash
curl -X POST http://localhost:8080/api/files/upload-images \
  -H "Authorization: Bearer {TU_TOKEN_AQUI}" \
  -F "files=@imagen1.jpg" \
  -F "files=@imagen2.jpg" \
  -F "files=@imagen3.jpg"
```

**Respuesta**:
```json
{
  "success": true,
  "message": "Imágenes subidas exitosamente",
  "data": {
    "urls": [
      "/images/uuid1.jpg",
      "/images/uuid2.jpg",
      "/images/uuid3.jpg"
    ]
  }
}
```

---

## Validaciones de Negocio

### ❌ Intentar crear propiedad sin ser anfitrión

```bash
# Login como viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "juan.traveler@example.com", "password": "Admin123!"}'

# Intentar crear propiedad
curl -X POST http://localhost:8080/api/propiedades \
  -H "Authorization: Bearer {TOKEN_VIAJERO}" \
  -d '{...}'
```

**Respuesta Esperada (403 Forbidden)**:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### ❌ Actualizar propiedad de otro usuario

```bash
# Intentar actualizar propiedad ID 1 (de Carlos) con token de María
curl -X PUT http://localhost:8080/api/propiedades/1 \
  -H "Authorization: Bearer {TOKEN_MARIA}" \
  -d '{...}'
```

**Respuesta Esperada (403 Forbidden)**:
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "No tienes permiso para actualizar esta propiedad"
}
```

### ❌ Crear propiedad con datos inválidos

```bash
curl -X POST http://localhost:8080/api/propiedades \
  -H "Authorization: Bearer {TOKEN_ANFITRION}" \
  -d '{
    "titulo": "Corto",
    "descripcion": "Muy corta",
    "precioPorNoche": -100
  }'
```

**Respuesta Esperada (400 Bad Request)**:
```json
{
  "status": 400,
  "error": "Validation Error",
  "message": "{titulo=El título debe tener entre 10 y 100 caracteres, descripcion=La descripción debe tener entre 50 y 2000 caracteres, precioPorNoche=El precio por noche debe ser mayor a 0}"
}
```

---

## Probar con Swagger UI

1. Abrir: http://localhost:8080/swagger-ui.html
2. Hacer login desde el controlador de Autenticación
3. Copiar el `accessToken`
4. Clic en **Authorize** (candado verde arriba a la derecha)
5. Pegar: `Bearer {accessToken}`
6. Expandir "Propiedades" y probar los endpoints

---

## Paginación y Ordenamiento

### Listar con paginación personalizada

```bash
# Página 1, 5 elementos, ordenar por precio ascendente
curl -X GET "http://localhost:8080/api/propiedades?page=1&size=5&sort=precioPorNoche&direction=ASC"

# Ordenar por puntuación descendente
curl -X GET "http://localhost:8080/api/propiedades?page=0&size=20&sort=puntuacionPromedio&direction=DESC"

# Ordenar por fecha de creación (más recientes)
curl -X GET "http://localhost:8080/api/propiedades?page=0&size=10&sort=createdDate&direction=DESC"
```

---

## Propiedades de Prueba Precargadas

La migración V10 incluye 4 propiedades de ejemplo:

1. **Casa de playa en Cartagena** (ID: 1) - Anfitrión: Carlos
2. **Apartamento en El Poblado, Medellín** (ID: 2) - Anfitrión: Carlos  
3. **Villa campestre en Pereira** (ID: 3) - Anfitrión: María
4. **Loft moderno en Bogotá** (ID: 4) - Anfitrión: María

Todas tienen estado `ACTIVA` y pueden ser consultadas públicamente.

---

## Verificar Almacenamiento de Archivos

Los archivos subidos se guardan en:
```
./uploads/
  ├── images/       (imágenes de propiedades)
  └── documents/    (documentos legales)
```

Accesibles vía HTTP:
- http://localhost:8080/images/{nombre-archivo}
- http://localhost:8080/documents/{nombre-archivo}

---

## Próximos Pasos

Con el CRUD de propiedades completo, el siguiente paso es:
- **Paso 6**: Implementar RF03 - Sistema de búsqueda y filtrado de propiedades
