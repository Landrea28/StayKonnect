package com.staykonnect.dto.pago;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para iniciar el proceso de pago de una reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IniciarPagoRequest {

    @NotNull(message = "El ID de la reserva es obligatorio")
    private Long reservaId;

    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago; // "card", "bank_transfer", etc.
}
