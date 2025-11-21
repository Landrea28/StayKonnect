package com.staykonnect.service;

import com.staykonnect.common.exception.BadRequestException;
import com.staykonnect.common.exception.ForbiddenException;
import com.staykonnect.common.exception.ResourceNotFoundException;
import com.staykonnect.domain.entity.Pago;
import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.EstadoPago;
import com.staykonnect.domain.enums.EstadoReserva;
import com.staykonnect.domain.repository.PagoRepository;
import com.staykonnect.domain.repository.ReservaRepository;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.pago.IniciarPagoRequest;
import com.staykonnect.dto.pago.PagoDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para gestión de pagos con Stripe.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.currency:cop}")
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
        log.info("Stripe API inicializada con moneda: {}", currency);
    }

    /**
     * Inicia el proceso de pago para una reserva confirmada.
     */
    @Transactional
    public PagoDTO iniciarPago(IniciarPagoRequest request) {
        log.info("Iniciando pago para reserva ID: {}", request.getReservaId());

        Usuario viajero = getUsuarioAutenticado();
        
        // Obtener reserva
        Reserva reserva = reservaRepository.findById(request.getReservaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el usuario sea el viajero de la reserva
        if (!reserva.getViajero().getId().equals(viajero.getId())) {
            throw new ForbiddenException("Solo el viajero de la reserva puede realizar el pago");
        }

        // Validar estado de la reserva
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new BadRequestException("Solo se pueden pagar reservas confirmadas");
        }

        // Verificar que no exista un pago exitoso previo
        boolean tienePagoExitoso = pagoRepository.existsByReservaIdAndEstado(
                reserva.getId(), 
                EstadoPago.COMPLETADO
        );
        if (tienePagoExitoso) {
            throw new BadRequestException("Esta reserva ya ha sido pagada");
        }

        try {
            // Convertir monto a centavos (Stripe requiere enteros)
            Long amountInCents = reserva.getPrecioTotal()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            // Crear PaymentIntent en Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .addPaymentMethodType(request.getMetodoPago())
                    .setDescription(String.format("Reserva #%d - %s", 
                            reserva.getId(), 
                            reserva.getPropiedad().getTitulo()))
                    .putMetadata("reserva_id", reserva.getId().toString())
                    .putMetadata("viajero_id", viajero.getId().toString())
                    .putMetadata("viajero_email", viajero.getEmail())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Crear registro de pago en BD
            Pago pago = Pago.builder()
                    .reserva(reserva)
                    .monto(reserva.getPrecioTotal())
                    .moneda(currency.toUpperCase())
                    .estado(EstadoPago.PENDIENTE)
                    .metodoPago(request.getMetodoPago())
                    .transaccionId(paymentIntent.getId())
                    .build();

            pago = pagoRepository.save(pago);
            log.info("Pago iniciado exitosamente. ID: {}, PaymentIntent: {}", pago.getId(), paymentIntent.getId());

            // Convertir a DTO y agregar client_secret
            PagoDTO pagoDTO = convertirADTO(pago);
            pagoDTO.setClientSecret(paymentIntent.getClientSecret());

            return pagoDTO;

        } catch (StripeException e) {
            log.error("Error al crear PaymentIntent en Stripe: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar el pago: " + e.getMessage());
        }
    }

    /**
     * Confirma el pago tras recibir webhook de Stripe.
     */
    @Transactional
    public void confirmarPago(String paymentIntentId) {
        log.info("Confirmando pago para PaymentIntent: {}", paymentIntentId);

        Pago pago = pagoRepository.findByTransaccionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        if (pago.getEstado() == EstadoPago.COMPLETADO) {
            log.warn("El pago {} ya estaba marcado como completado", pago.getId());
            return;
        }

        // Actualizar pago
        pago.setEstado(EstadoPago.COMPLETADO);
        pago.setFechaPago(LocalDateTime.now());
        pagoRepository.save(pago);

        // Actualizar estado de reserva a PAGADA
        Reserva reserva = pago.getReserva();
        reserva.setEstado(EstadoReserva.PAGADA);
        reservaRepository.save(reserva);

        log.info("Pago confirmado exitosamente. Reserva {} actualizada a estado PAGADA", reserva.getId());
    }

    /**
     * Marca un pago como fallido.
     */
    @Transactional
    public void marcarPagoFallido(String paymentIntentId, String motivoFallo) {
        log.info("Marcando pago como fallido para PaymentIntent: {}", paymentIntentId);

        Pago pago = pagoRepository.findByTransaccionId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        pago.setEstado(EstadoPago.FALLIDO);
        pagoRepository.save(pago);

        log.info("Pago {} marcado como fallido. Motivo: {}", pago.getId(), motivoFallo);
    }

    /**
     * Procesa reembolso para una reserva cancelada.
     */
    @Transactional
    public PagoDTO procesarReembolso(Long reservaId) {
        log.info("Procesando reembolso para reserva ID: {}", reservaId);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Buscar pago completado
        Pago pagoOriginal = pagoRepository.findByReservaIdAndEstado(reservaId, EstadoPago.COMPLETADO)
                .orElseThrow(() -> new BadRequestException("No existe un pago completado para esta reserva"));

        // Validar que la reserva esté cancelada
        if (reserva.getEstado() != EstadoReserva.CANCELADA) {
            throw new BadRequestException("Solo se pueden reembolsar reservas canceladas");
        }

        try {
            // Crear reembolso en Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(pagoOriginal.getTransaccionId());
            
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("payment_intent", paymentIntent.getId());
            
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(refundParams);

            // Crear registro de pago de reembolso
            Pago pagoReembolso = Pago.builder()
                    .reserva(reserva)
                    .monto(pagoOriginal.getMonto().negate()) // Monto negativo para reembolso
                    .moneda(pagoOriginal.getMoneda())
                    .estado(EstadoPago.REEMBOLSADO)
                    .metodoPago(pagoOriginal.getMetodoPago())
                    .transaccionId(refund.getId())
                    .fechaPago(LocalDateTime.now())
                    .build();

            pagoReembolso = pagoRepository.save(pagoReembolso);

            // Actualizar pago original
            pagoOriginal.setEstado(EstadoPago.REEMBOLSADO);
            pagoRepository.save(pagoOriginal);

            log.info("Reembolso procesado exitosamente. Refund ID: {}", refund.getId());
            return convertirADTO(pagoReembolso);

        } catch (StripeException e) {
            log.error("Error al procesar reembolso en Stripe: {}", e.getMessage(), e);
            throw new BadRequestException("Error al procesar el reembolso: " + e.getMessage());
        }
    }

    /**
     * Obtiene el estado de un pago por reserva.
     */
    @Transactional(readOnly = true)
    public PagoDTO obtenerPagoPorReserva(Long reservaId) {
        Usuario usuario = getUsuarioAutenticado();
        
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar permisos
        boolean esViajero = reserva.getViajero().getId().equals(usuario.getId());
        boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(usuario.getId());

        if (!esViajero && !esAnfitrion) {
            throw new ForbiddenException("No tienes permiso para ver el pago de esta reserva");
        }

        Pago pago = pagoRepository.findByReservaIdAndEstado(reservaId, EstadoPago.COMPLETADO)
                .or(() -> pagoRepository.findByReservaIdAndEstado(reservaId, EstadoPago.PENDIENTE))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró información de pago"));

        return convertirADTO(pago);
    }

    /**
     * Verifica el estado de un PaymentIntent en Stripe.
     */
    @Transactional(readOnly = true)
    public String verificarEstadoPago(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.getStatus();
        } catch (StripeException e) {
            log.error("Error al verificar estado de pago en Stripe: {}", e.getMessage());
            throw new BadRequestException("Error al verificar el estado del pago");
        }
    }

    /**
     * Obtiene el usuario autenticado.
     */
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Convierte una entidad Pago a PagoDTO.
     */
    private PagoDTO convertirADTO(Pago pago) {
        return modelMapper.map(pago, PagoDTO.class);
    }
}
