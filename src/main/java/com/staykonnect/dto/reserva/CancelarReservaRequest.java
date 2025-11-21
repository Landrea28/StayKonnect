package com.staykonnect.dto.reserva;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de cancelación de reserva.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarReservaRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivoCancelacion;
}
