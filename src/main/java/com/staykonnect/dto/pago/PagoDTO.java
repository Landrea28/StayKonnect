package com.staykonnect.dto.pago;

import com.staykonnect.domain.enums.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con información del pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {

    private Long id;
    private Long reservaId;
    
    private BigDecimal monto;
    private String moneda;
    private EstadoPago estado;
    
    private String metodoPago;
    private String transaccionId; // ID de Stripe PaymentIntent
    private String clientSecret; // Para completar el pago en el frontend
    
    private LocalDateTime fechaPago;
    private LocalDateTime createdDate;
}
