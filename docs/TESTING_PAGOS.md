# Guía de Testing - Sistema de Pagos con Stripe

Esta guía describe cómo probar la integración de pagos con Stripe.

## Prerrequisitos

1. **Cuenta de Stripe** (modo test)
   - Regístrate en https://stripe.com
   - Obtén tus claves de prueba desde el Dashboard

2. **Configuración de claves**
   - Actualiza `application-dev.properties`:
   ```properties
   stripe.api.key=sk_test_TU_CLAVE_SECRETA_TEST
   stripe.webhook.secret=whsec_TU_WEBHOOK_SECRET
   stripe.currency=cop
   ```

3. **Stripe CLI** (para webhooks locales)
   ```bash
   # Instalar Stripe CLI
   scoop install stripe
   
   # O descargar desde https://stripe.com/docs/stripe-cli
   ```

4. **Backend corriendo** en `http://localhost:8080`

5. **Usuario viajero** con token JWT

## Configuración Inicial

### 1. Obtener Claves de Stripe (Test Mode)

1. Ve a https://dashboard.stripe.com/test/dashboard
2. En **Developers > API keys**:
   - Copia **Secret key** (sk_test_...)
   - Copia **Publishable key** (pk_test_...)

### 2. Configurar Webhooks Locales

```bash
# Login en Stripe CLI
stripe login

# Redirigir webhooks a tu servidor local
stripe listen --forward-to localhost:8080/api/pagos/webhook

# Guarda el webhook signing secret (whsec_...)
```

## Flujo Completo de Pago

### Paso 1: Crear y Confirmar Reserva

```bash
# Login como viajero
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.viajera@test.com","password":"Test1234@"}' | jq

export TOKEN="tu_token_aqui"

# Crear reserva
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId": 1,
    "fechaCheckin": "2024-12-01",
    "fechaCheckout": "2024-12-10",
    "numeroHuespedes": 4
  }' | jq

# Login como anfitrión y confirmar
# (ver guía TESTING_RESERVAS.md)
```

### Paso 2: Iniciar Pago

```bash
# Iniciar proceso de pago (viajero)
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 1,
    "metodoPago": "card"
  }' | jq
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Pago iniciado exitosamente. Usa el client_secret para completar el pago.",
  "data": {
    "id": 1,
    "reservaId": 1,
    "monto": 3465000,
    "moneda": "COP",
    "estado": "PENDIENTE",
    "metodoPago": "card",
    "transaccionId": "pi_1234567890abcdef",
    "clientSecret": "pi_1234567890abcdef_secret_xyz123",
    "fechaPago": null,
    "createdDate": "2024-11-19T10:30:00"
  }
}
```

**Importante:** Guarda el `clientSecret` para el siguiente paso.

### Paso 3: Completar Pago (Frontend)

En producción, el frontend usa el `clientSecret` con Stripe Elements o Stripe.js:

```javascript
// Ejemplo con Stripe.js
const stripe = Stripe('pk_test_TU_PUBLISHABLE_KEY');

const {error} = await stripe.confirmCardPayment(clientSecret, {
  payment_method: {
    card: cardElement,
    billing_details: {
      name: 'Maria Lopez',
      email: 'maria.viajera@test.com'
    }
  }
});

if (error) {
  console.error('Error:', error.message);
} else {
  console.log('Pago exitoso!');
}
```

### Paso 4: Simular Pago desde Stripe CLI

Para testing sin frontend:

```bash
# Simular pago exitoso
stripe payment_intents confirm pi_1234567890abcdef \
  --payment-method=pm_card_visa

# Esto dispara el webhook payment_intent.succeeded
```

### Paso 5: Verificar Estado

```bash
# Verificar que el pago se marcó como COMPLETADO
curl -X GET http://localhost:8080/api/pagos/reserva/1 \
  -H "Authorization: Bearer $TOKEN" | jq

# Verificar que la reserva cambió a estado PAGADA
curl -X GET http://localhost:8080/api/reservas/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Endpoints Disponibles

### 1. Iniciar Pago (POST /api/pagos/iniciar)

**Permisos:** Solo VIAJERO

```bash
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reservaId": 1,
    "metodoPago": "card"
  }' | jq
```

**Validaciones:**
- ✅ Usuario es el viajero de la reserva
- ✅ Reserva está en estado CONFIRMADA
- ✅ No existe un pago completado previo
- ✅ Crea PaymentIntent en Stripe
- ✅ Convierte monto a centavos (COP × 100)

**Métodos de pago soportados:**
- `card` - Tarjetas de crédito/débito
- `bank_transfer` - Transferencia bancaria
- Otros métodos según configuración de Stripe

### 2. Webhook de Stripe (POST /api/pagos/webhook)

**Permisos:** Público (pero validado con firma)

Este endpoint **NO** se llama manualmente. Stripe lo invoca automáticamente.

**Eventos manejados:**
- `payment_intent.succeeded` - Pago exitoso
- `payment_intent.payment_failed` - Pago fallido

**Testing con Stripe CLI:**
```bash
# Simular evento de pago exitoso
stripe trigger payment_intent.succeeded
```

### 3. Obtener Pago por Reserva (GET /api/pagos/reserva/{reservaId})

**Permisos:** VIAJERO o ANFITRION (de la reserva)

```bash
curl -X GET http://localhost:8080/api/pagos/reserva/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 4. Procesar Reembolso (POST /api/pagos/reembolso/{reservaId})

**Permisos:** VIAJERO, ANFITRION o ADMIN

```bash
# Primero cancelar la reserva
curl -X PUT http://localhost:8080/api/reservas/1/cancelar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"motivoCancelacion":"Cambio de planes"}'

# Luego procesar reembolso
curl -X POST http://localhost:8080/api/pagos/reembolso/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Validaciones:**
- ✅ Existe un pago COMPLETADO
- ✅ Reserva está CANCELADA
- ✅ Crea reembolso en Stripe
- ✅ Registra pago negativo (reembolso)
- ✅ Actualiza pago original a REEMBOLSADO

### 5. Verificar Estado en Stripe (GET /api/pagos/verificar/{paymentIntentId})

**Permisos:** VIAJERO, ANFITRION o ADMIN

```bash
curl -X GET http://localhost:8080/api/pagos/verificar/pi_1234567890abcdef \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Estados posibles de Stripe:**
- `requires_payment_method` - Necesita método de pago
- `requires_confirmation` - Necesita confirmación
- `processing` - Procesando
- `succeeded` - Exitoso
- `canceled` - Cancelado

## Tarjetas de Prueba (Stripe Test Mode)

### Tarjetas Exitosas

```
Número: 4242 4242 4242 4242
Fecha: Cualquier fecha futura
CVV: Cualquier 3 dígitos
```

### Tarjetas con Errores Específicos

```
# Pago rechazado
4000 0000 0000 0002

# Fondos insuficientes
4000 0000 0000 9995

# Requiere autenticación 3D Secure
4000 0025 0000 3155

# Expirada
4000 0000 0000 0069
```

## Casos de Prueba

### Escenario 1: Pago Exitoso Completo

```bash
# 1. Crear reserva y confirmar
# 2. Iniciar pago
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reservaId":1,"metodoPago":"card"}'

# 3. Simular pago exitoso
stripe payment_intents confirm <PAYMENT_INTENT_ID> \
  --payment-method=pm_card_visa

# 4. Verificar estado
curl -X GET http://localhost:8080/api/pagos/reserva/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.data.estado'
# Debe ser: "COMPLETADO"

curl -X GET http://localhost:8080/api/reservas/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.data.estado'
# Debe ser: "PAGADA"
```

### Escenario 2: Pago Fallido

```bash
# 1. Iniciar pago
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reservaId":1,"metodoPago":"card"}'

# 2. Simular rechazo
stripe payment_intents confirm <PAYMENT_INTENT_ID> \
  --payment-method=pm_card_chargeDeclined

# 3. Verificar estado
curl -X GET http://localhost:8080/api/pagos/reserva/1 \
  -H "Authorization: Bearer $TOKEN" | jq '.data.estado'
# Debe ser: "FALLIDO"
```

### Escenario 3: Reembolso por Cancelación

```bash
# 1. Pago exitoso (escenario 1)
# 2. Cancelar reserva
curl -X PUT http://localhost:8080/api/reservas/1/cancelar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"motivoCancelacion":"No puedo viajar"}'

# 3. Procesar reembolso
curl -X POST http://localhost:8080/api/pagos/reembolso/1 \
  -H "Authorization: Bearer $TOKEN" | jq

# 4. Verificar reembolso
curl -X GET http://localhost:8080/api/pagos/reserva/1 \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Escenario 4: Intento de Doble Pago

```bash
# 1. Pago exitoso
# 2. Intentar pagar nuevamente
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reservaId":1,"metodoPago":"card"}'

# ❌ Error: "Esta reserva ya ha sido pagada"
```

### Escenario 5: Pagar Reserva No Confirmada

```bash
# 1. Crear reserva (sin confirmar)
curl -X POST http://localhost:8080/api/reservas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "propiedadId":1,
    "fechaCheckin":"2024-12-15",
    "fechaCheckout":"2024-12-20",
    "numeroHuespedes":2
  }'

# 2. Intentar pagar (estado PENDIENTE)
curl -X POST http://localhost:8080/api/pagos/iniciar \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reservaId":2,"metodoPago":"card"}'

# ❌ Error: "Solo se pueden pagar reservas confirmadas"
```

## Monitoreo en Stripe Dashboard

1. Ve a https://dashboard.stripe.com/test/payments
2. Verás todos los PaymentIntents creados
3. Cada pago incluye:
   - Monto y moneda
   - Estado actual
   - Metadata (reserva_id, viajero_id, etc.)
   - Eventos asociados

## Webhooks en Producción

### Configurar Webhook Endpoint

1. Ve a https://dashboard.stripe.com/test/webhooks
2. Clic en **"Add endpoint"**
3. URL: `https://tu-dominio.com/api/pagos/webhook`
4. Selecciona eventos:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
5. Guarda el **Signing secret** (whsec_...)

### Verificar Webhooks

```bash
# Ver logs de webhooks en Stripe CLI
stripe listen

# Ver eventos específicos
stripe events list --limit 10
```

## Errores Comunes

### 400 Bad Request - Stripe API Key inválida

```json
{
  "success": false,
  "message": "Error al procesar el pago: Invalid API Key provided"
}
```

**Solución:** Verifica que `stripe.api.key` sea correcta en `application-dev.properties`

### 400 Bad Request - Webhook signature inválida

```
Invalid signature
```

**Solución:** Actualiza `stripe.webhook.secret` con el valor correcto del Stripe CLI o Dashboard

### 403 Forbidden - Usuario no autorizado

```json
{
  "success": false,
  "message": "Solo el viajero de la reserva puede realizar el pago"
}
```

**Solución:** Usa el token del usuario viajero que creó la reserva

### 400 Bad Request - Monto inválido

```json
{
  "success": false,
  "message": "Error al procesar el pago: Amount must be at least $0.50 cop"
}
```

**Solución:** Stripe requiere un monto mínimo según la moneda

## Testing con Postman

1. Importa la colección de endpoints
2. Configura variables de entorno:
   - `base_url`: http://localhost:8080
   - `token_viajero`: JWT del viajero
   - `reserva_id`: ID de la reserva

3. Ejecuta la colección en orden:
   - Crear reserva
   - Confirmar reserva (como anfitrión)
   - Iniciar pago
   - Simular webhook (Stripe CLI)
   - Verificar estado

## Swagger UI

Accede a `http://localhost:8080/swagger-ui/index.html`

1. Busca **"Pagos"**
2. Haz clic en **"Authorize"** e ingresa tu JWT
3. Prueba endpoints:
   - POST /api/pagos/iniciar
   - GET /api/pagos/reserva/{reservaId}

## Próximos Pasos

Una vez validado el sistema de pagos:

- **Paso 9:** Sistema de mensajería (chat viajero-anfitrión)
- **Paso 10:** Sistema de valoraciones y reputación
- **Paso 11:** Panel de administración

## Recursos Adicionales

- [Stripe Testing Guide](https://stripe.com/docs/testing)
- [Stripe CLI Docs](https://stripe.com/docs/stripe-cli)
- [PaymentIntents API](https://stripe.com/docs/api/payment_intents)
- [Webhooks Guide](https://stripe.com/docs/webhooks)
