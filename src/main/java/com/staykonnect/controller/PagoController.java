package com.staykonnect.controller;

import com.staykonnect.common.dto.ApiResponse;
import com.staykonnect.dto.pago.IniciarPagoRequest;
import com.staykonnect.dto.pago.PagoDTO;
import com.staykonnect.service.PagoService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de pagos.
 */
@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Endpoints para gestión de pagos con Stripe")
public class PagoController {

    private final PagoService pagoService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Inicia el proceso de pago para una reserva confirmada.
     */
    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('VIAJERO')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Iniciar pago", description = "Crea un PaymentIntent en Stripe para pagar una reserva")
    public ResponseEntity<ApiResponse<PagoDTO>> iniciarPago(@Valid @RequestBody IniciarPagoRequest request) {
        PagoDTO pago = pagoService.iniciarPago(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Pago iniciado exitosamente. Usa el client_secret para completar el pago.", 
                pago
        ));
    }

    /**
     * Webhook para recibir eventos de Stripe.
     */
    @PostMapping("/webhook")
    @Operation(summary = "Webhook de Stripe", description = "Endpoint para recibir notificaciones de eventos de pago")
    public ResponseEntity<String> webhookStripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        log.info("Webhook recibido de Stripe");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        // Procesar el evento
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);
                
                if (paymentIntent != null) {
                    log.info("PaymentIntent succeeded: {}", paymentIntent.getId());
                    pagoService.confirmarPago(paymentIntent.getId());
                }
                break;

            case "payment_intent.payment_failed":
                PaymentIntent failedPaymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);
                
                if (failedPaymentIntent != null) {
                    log.warn("PaymentIntent failed: {}", failedPaymentIntent.getId());
                    String errorMessage = failedPaymentIntent.getLastPaymentError() != null
                            ? failedPaymentIntent.getLastPaymentError().getMessage()
                            : "Unknown error";
                    pagoService.marcarPagoFallido(failedPaymentIntent.getId(), errorMessage);
                }
                break;

            default:
                log.info("Evento no manejado: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * Obtiene información del pago de una reserva.
     */
    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener pago por reserva", description = "Obtiene la información del pago de una reserva")
    public ResponseEntity<ApiResponse<PagoDTO>> obtenerPagoPorReserva(@PathVariable Long reservaId) {
        PagoDTO pago = pagoService.obtenerPagoPorReserva(reservaId);
        return ResponseEntity.ok(ApiResponse.success("Información de pago obtenida exitosamente", pago));
    }

    /**
     * Procesa reembolso para una reserva cancelada.
     */
    @PostMapping("/reembolso/{reservaId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Procesar reembolso", description = "Procesa el reembolso de una reserva cancelada")
    public ResponseEntity<ApiResponse<PagoDTO>> procesarReembolso(@PathVariable Long reservaId) {
        PagoDTO reembolso = pagoService.procesarReembolso(reservaId);
        return ResponseEntity.ok(ApiResponse.success("Reembolso procesado exitosamente", reembolso));
    }

    /**
     * Verifica el estado de un pago en Stripe.
     */
    @GetMapping("/verificar/{paymentIntentId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Verificar estado de pago", description = "Verifica el estado actual de un pago en Stripe")
    public ResponseEntity<ApiResponse<String>> verificarEstadoPago(@PathVariable String paymentIntentId) {
        String estado = pagoService.verificarEstadoPago(paymentIntentId);
        return ResponseEntity.ok(ApiResponse.success("Estado verificado", estado));
    }
}
