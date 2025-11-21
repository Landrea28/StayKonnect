# Guía de Testing - Sistema de Mensajería con WebSockets

Esta guía describe cómo probar el sistema de mensajería en tiempo real.

## Prerrequisitos

1. **Backend corriendo** en `http://localhost:8080`
2. **Dos usuarios** autenticados (uno viajero, uno anfitrión)
3. **Cliente WebSocket** (navegador, Postman, o librería)

## Arquitectura del Sistema

### Tecnologías Utilizadas
- **STOMP over WebSocket**: Protocolo de mensajería
- **SockJS**: Fallback a polling si WebSocket no disponible
- **Spring Messaging**: SimpleBroker para pub/sub
- **JWT Authentication**: Autenticación en conexión WebSocket

### Endpoints WebSocket

```
ws://localhost:8080/ws         # WebSocket puro
ws://localhost:8080/ws/sockjs  # WebSocket + SockJS fallback
```

### Canales STOMP

```
# Cliente → Servidor
/app/chat.enviarMensaje    # Enviar mensaje (deprecado, usar REST)

# Servidor → Cliente (suscripciones)
/user/queue/mensajes       # Mensajes privados para el usuario
/topic/notificaciones      # Notificaciones globales
```

## Configuración Inicial

### 1. Autenticar Usuarios

```bash
# Usuario 1: Viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.viajera@test.com","password":"Test1234@"}' | jq

export TOKEN_VIAJERO="tu_token_aqui"

# Usuario 2: Anfitrión
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"carlos.anfitrion@test.com","password":"Test1234@"}' | jq

export TOKEN_ANFITRION="tu_token_aqui"
```

## Testing con REST API

### Endpoint 1: Enviar Mensaje (POST /api/mensajes)

```bash
# Viajero envía mensaje a anfitrión
curl -X POST http://localhost:8080/api/mensajes \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarioId": 2,
    "contenido": "Hola! Tengo algunas preguntas sobre tu propiedad",
    "reservaId": null
  }' | jq
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Mensaje enviado exitosamente",
  "data": {
    "id": 1,
    "contenido": "Hola! Tengo algunas preguntas sobre tu propiedad",
    "leido": false,
    "fechaLectura": null,
    "createdDate": "2024-11-19T15:30:00",
    "remitenteId": 1,
    "remitenteNombre": "María López",
    "remitenteEmail": "maria.viajera@test.com",
    "remitenteFotoPerfil": null,
    "destinatarioId": 2,
    "destinatarioNombre": "Carlos García",
    "destinatarioEmail": "carlos.anfitrion@test.com",
    "destinatarioFotoPerfil": null,
    "reservaId": null,
    "reservaTitulo": null
  }
}
```

### Endpoint 2: Listar Conversaciones (GET /api/mensajes/conversaciones)

```bash
# Obtener todas las conversaciones del usuario actual
curl -X GET "http://localhost:8080/api/mensajes/conversaciones?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Conversaciones obtenidas exitosamente",
  "data": {
    "content": [
      {
        "otroUsuarioId": 2,
        "otroUsuarioNombre": "Carlos García",
        "otroUsuarioEmail": "carlos.anfitrion@test.com",
        "otroUsuarioFotoPerfil": null,
        "ultimoMensajeId": 3,
        "ultimoMensajeContenido": "Claro! Pregunta lo que necesites",
        "ultimoMensajeFecha": "2024-11-19T15:35:00",
        "ultimoMensajeLeido": false,
        "yoEnvieUltimoMensaje": false,
        "mensajesNoLeidos": 1
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

### Endpoint 3: Obtener Conversación (GET /api/mensajes/conversacion/{otroUsuarioId})

```bash
# Obtener historial de mensajes con un usuario específico
curl -X GET "http://localhost:8080/api/mensajes/conversacion/2?page=0&size=50" \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

### Endpoint 4: Marcar Mensaje Como Leído (PUT /api/mensajes/{mensajeId}/leer)

```bash
# Marcar un mensaje específico como leído
curl -X PUT http://localhost:8080/api/mensajes/1/leer \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

### Endpoint 5: Marcar Conversación Como Leída (PUT /api/mensajes/conversacion/{otroUsuarioId}/leer)

```bash
# Marcar todos los mensajes de una conversación como leídos
curl -X PUT http://localhost:8080/api/mensajes/conversacion/1/leer \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq
```

### Endpoint 6: Contar Mensajes No Leídos (GET /api/mensajes/no-leidos/count)

```bash
# Obtener badge de mensajes no leídos
curl -X GET http://localhost:8080/api/mensajes/no-leidos/count \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Cantidad de mensajes no leídos",
  "data": 5
}
```

### Endpoint 7: Mensajes por Reserva (GET /api/mensajes/reserva/{reservaId})

```bash
# Obtener mensajes relacionados con una reserva específica
curl -X GET http://localhost:8080/api/mensajes/reserva/1 \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

## Testing con WebSocket (Tiempo Real)

### Opción 1: Cliente JavaScript (Browser)

```html
<!DOCTYPE html>
<html>
<head>
    <title>StayKonnect Chat Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>Chat en Tiempo Real</h1>
    <div id="status">Desconectado</div>
    <div id="mensajes"></div>

    <script>
        const token = 'TU_JWT_TOKEN';
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);

        // Conectar con autenticación
        stompClient.connect(
            {'Authorization': 'Bearer ' + token},
            function(frame) {
                console.log('Conectado:', frame);
                document.getElementById('status').textContent = 'Conectado';

                // Suscribirse a mensajes privados
                stompClient.subscribe('/user/queue/mensajes', function(message) {
                    const msg = JSON.parse(message.body);
                    console.log('Mensaje recibido:', msg);
                    mostrarMensaje(msg);
                });
            },
            function(error) {
                console.error('Error de conexión:', error);
                document.getElementById('status').textContent = 'Error: ' + error;
            }
        );

        function mostrarMensaje(msg) {
            const div = document.getElementById('mensajes');
            const p = document.createElement('p');
            
            if (msg.tipo === 'MENSAJE_NUEVO') {
                p.textContent = `${msg.remitenteNombre}: ${msg.contenido}`;
            } else if (msg.tipo === 'MENSAJE_LEIDO') {
                p.textContent = `✓✓ Mensaje ${msg.id} leído`;
                p.style.color = 'blue';
            }
            
            div.appendChild(p);
        }
    </script>
</body>
</html>
```

### Opción 2: Python con websocket-client

```python
import json
import websocket
import threading

TOKEN = "tu_jwt_token_aqui"

def on_message(ws, message):
    print(f"📩 Mensaje recibido: {message}")
    data = json.loads(message)
    
    if 'body' in data:
        body = json.loads(data['body'])
        if body['tipo'] == 'MENSAJE_NUEVO':
            print(f"💬 {body['remitenteNombre']}: {body['contenido']}")
        elif body['tipo'] == 'MENSAJE_LEIDO':
            print(f"✓✓ Mensaje {body['id']} leído")

def on_error(ws, error):
    print(f"❌ Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print("🔌 Desconectado")

def on_open(ws):
    print("✅ Conectado al servidor WebSocket")
    
    # Enviar CONNECT frame con JWT
    connect_frame = f"""CONNECT
Authorization:Bearer {TOKEN}
accept-version:1.1,1.0
heart-beat:10000,10000

\x00"""
    ws.send(connect_frame)
    
    # Suscribirse a mensajes privados
    subscribe_frame = """SUBSCRIBE
id:sub-0
destination:/user/queue/mensajes

\x00"""
    ws.send(subscribe_frame)

# Conectar
ws = websocket.WebSocketApp(
    "ws://localhost:8080/ws",
    on_open=on_open,
    on_message=on_message,
    on_error=on_error,
    on_close=on_close
)

ws.run_forever()
```

### Opción 3: Postman

1. **Crear nueva request WebSocket**
   - URL: `ws://localhost:8080/ws`
   - Agregar header: `Authorization: Bearer {token}`

2. **Enviar CONNECT frame**
   ```
   CONNECT
   Authorization:Bearer tu_token_aqui
   accept-version:1.1
   
   ^@
   ```

3. **Suscribirse a mensajes**
   ```
   SUBSCRIBE
   id:sub-0
   destination:/user/queue/mensajes
   
   ^@
   ```

4. **Enviar mensaje** (usar REST API en paralelo)

## Flujo Completo de Testing

### Escenario 1: Chat Básico

```bash
# Terminal 1: Viajero se conecta a WebSocket
# (usar cliente WebSocket JavaScript o Python)

# Terminal 2: Viajero envía mensaje por REST
curl -X POST http://localhost:8080/api/mensajes \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarioId": 2,
    "contenido": "Hola! ¿El departamento está disponible en diciembre?"
  }'

# Terminal 3: Anfitrión se conecta a WebSocket y recibe mensaje en tiempo real
# Anfitrión responde
curl -X POST http://localhost:8080/api/mensajes \
  -H "Authorization: Bearer $TOKEN_ANFITRION" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarioId": 1,
    "contenido": "Sí! Está disponible del 1 al 20 de diciembre"
  }'

# Viajero recibe respuesta en tiempo real por WebSocket
```

### Escenario 2: Chat sobre Reserva

```bash
# 1. Crear reserva
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId": 1,
    "fechaCheckin": "2024-12-01",
    "fechaCheckout": "2024-12-10",
    "numeroHuespedes": 4
  }' | jq -r '.data.id'

export RESERVA_ID=1

# 2. Enviar mensaje relacionado con la reserva
curl -X POST http://localhost:8080/api/mensajes \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{
    "destinatarioId": 2,
    "contenido": "¿A qué hora puedo hacer check-in?",
    "reservaId": '$RESERVA_ID'
  }'

# 3. Ver mensajes de la reserva
curl -X GET http://localhost:8080/api/mensajes/reserva/$RESERVA_ID \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq
```

### Escenario 3: Marcar Mensajes Como Leídos

```bash
# 1. Anfitrión tiene mensajes no leídos
curl -X GET http://localhost:8080/api/mensajes/no-leidos/count \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq '.data'
# Output: 3

# 2. Anfitrión marca conversación como leída
curl -X PUT http://localhost:8080/api/mensajes/conversacion/1/leer \
  -H "Authorization: Bearer $TOKEN_ANFITRION"

# 3. Viajero recibe notificación WebSocket de "MENSAJE_LEIDO"

# 4. Verificar que el contador bajó
curl -X GET http://localhost:8080/api/mensajes/no-leidos/count \
  -H "Authorization: Bearer $TOKEN_ANFITRION" | jq '.data'
# Output: 0
```

### Escenario 4: Múltiples Conversaciones

```bash
# Crear mensajes con diferentes usuarios
for usuario_id in 2 3 4; do
  curl -X POST http://localhost:8080/api/mensajes \
    -H "Authorization: Bearer $TOKEN_VIAJERO" \
    -H "Content-Type: application/json" \
    -d '{
      "destinatarioId": '$usuario_id',
      "contenido": "Hola usuario '$usuario_id'"
    }'
  sleep 1
done

# Listar todas las conversaciones
curl -X GET http://localhost:8080/api/mensajes/conversaciones \
  -H "Authorization: Bearer $TOKEN_VIAJERO" | jq '.data.content'
```

## Eventos WebSocket

### Tipos de Eventos

```typescript
enum TipoEvento {
  MENSAJE_NUEVO,        // Nuevo mensaje recibido
  MENSAJE_LEIDO,        // Mensaje marcado como leído
  USUARIO_ESCRIBIENDO,  // (Futuro) Usuario está escribiendo
  USUARIO_CONECTADO,    // (Futuro) Usuario se conectó
  USUARIO_DESCONECTADO  // (Futuro) Usuario se desconectó
}
```

### Estructura de Evento MENSAJE_NUEVO

```json
{
  "id": 123,
  "contenido": "Hola! ¿Cómo estás?",
  "fecha": "2024-11-19T15:30:00",
  "remitenteId": 1,
  "remitenteNombre": "María López",
  "remitenteFotoPerfil": "https://...",
  "destinatarioId": 2,
  "tipo": "MENSAJE_NUEVO",
  "reservaId": null
}
```

### Estructura de Evento MENSAJE_LEIDO

```json
{
  "id": 123,
  "tipo": "MENSAJE_LEIDO",
  "destinatarioId": 2
}
```

## Validaciones del Sistema

### Reglas de Negocio

1. ✅ No se puede enviar mensaje a uno mismo
2. ✅ Solo el destinatario puede marcar como leído
3. ✅ Solo usuarios involucrados en reserva pueden ver esos mensajes
4. ✅ Viajeros y anfitriones pueden chatear (admins no tienen acceso)
5. ✅ Los mensajes se ordenan por fecha (DESC en listas, ASC en conversación)

### Casos de Error

```bash
# Error: Enviar mensaje a uno mismo
curl -X POST http://localhost:8080/api/mensajes \
  -H "Authorization: Bearer $TOKEN_VIAJERO" \
  -H "Content-Type: application/json" \
  -d '{"destinatarioId": 1, "contenido": "Test"}'
# ❌ "No puedes enviarte mensajes a ti mismo"

# Error: Marcar mensaje de otro como leído
curl -X PUT http://localhost:8080/api/mensajes/5/leer \
  -H "Authorization: Bearer $TOKEN_VIAJERO"
# ❌ "Solo el destinatario puede marcar el mensaje como leído"

# Error: Ver mensajes de reserva ajena
curl -X GET http://localhost:8080/api/mensajes/reserva/999 \
  -H "Authorization: Bearer $TOKEN_VIAJERO"
# ❌ "No tienes permiso para ver estos mensajes"
```

## Monitoreo y Debug

### Logs del Servidor

```bash
# Ver logs de WebSocket
tail -f logs/spring.log | grep "WebSocket\|Mensaje"
```

**Output esperado:**
```
2024-11-19 15:30:00 INFO  WebSocketAuthInterceptor : Usuario autenticado en WebSocket: maria.viajera@test.com
2024-11-19 15:30:05 INFO  MensajeService : Enviando mensaje de usuario 1 a usuario 2
2024-11-19 15:30:05 INFO  MensajeService : Mensaje 1 enviado exitosamente
2024-11-19 15:30:05 DEBUG MensajeService : Notificación WebSocket enviada a usuario carlos.anfitrion@test.com
```

### Verificar Conexiones WebSocket

```bash
# Ver conexiones activas (requiere Spring Boot Actuator)
curl http://localhost:8080/actuator/metrics/websocket.sessions.active | jq
```

## Testing con Frontend (Ejemplo React)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export class ChatService {
  constructor(token) {
    this.token = token;
    this.stompClient = null;
  }

  connect(onMessageReceived) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect(
      { 'Authorization': `Bearer ${this.token}` },
      () => {
        console.log('✅ Conectado al chat');
        
        // Suscribirse a mensajes privados
        this.stompClient.subscribe('/user/queue/mensajes', (message) => {
          const msg = JSON.parse(message.body);
          onMessageReceived(msg);
        });
      },
      (error) => {
        console.error('❌ Error de conexión:', error);
      }
    );
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}

// Uso
const chatService = new ChatService(localStorage.getItem('token'));
chatService.connect((mensaje) => {
  console.log('Nuevo mensaje:', mensaje);
  // Actualizar UI
});
```

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Mensajes"**
2. Autoriza con tu JWT
3. Prueba endpoints REST (WebSocket no soportado en Swagger)

## Rendimiento y Escalabilidad

### Métricas Recomendadas

- **Latencia de envío**: < 100ms
- **Latencia de entrega WebSocket**: < 50ms
- **Conexiones WebSocket concurrentes**: > 1000
- **Mensajes por segundo**: > 100

### Testing de Carga

```bash
# Usar herramientas como Artillery o k6
npm install -g artillery

# artillery.yml
config:
  target: "http://localhost:8080"
  phases:
    - duration: 60
      arrivalRate: 10
scenarios:
  - name: "Enviar mensajes"
    flow:
      - post:
          url: "/api/mensajes"
          headers:
            Authorization: "Bearer {{token}}"
          json:
            destinatarioId: 2
            contenido: "Test de carga"

# Ejecutar
artillery run artillery.yml
```

## Próximos Pasos

Una vez validado el sistema de mensajería:

- **Paso 10:** Sistema de valoraciones y reputación
- **Paso 11:** Panel de administración
- **Paso 12:** Notificaciones push

## Recursos Adicionales

- [Spring WebSocket Docs](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [STOMP Protocol](https://stomp.github.io/)
- [SockJS](https://github.com/sockjs/sockjs-client)
