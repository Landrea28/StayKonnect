package com.staykonnect.dto.reserva;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para solicitud de creación de reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearReservaRequest {

    @NotNull(message = "El ID de la propiedad es obligatorio")
    private Long propiedadId;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @Future(message = "La fecha de check-in debe ser futura")
    private LocalDate fechaCheckin;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @Future(message = "La fecha de check-out debe ser futura")
    private LocalDate fechaCheckout;

    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Max(value = 100, message = "El número máximo de huéspedes es 100")
    private Integer numeroHuespedes;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notasEspeciales;
}
