package com.staykonnect.dto.mensaje;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnviarMensajeRequest {

    @NotNull(message = "El destinatario es obligatorio")
    private Long destinatarioId;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    @Size(min = 1, max = 2000, message = "El mensaje debe tener entre 1 y 2000 caracteres")
    private String contenido;

    private Long reservaId; // Opcional: relacionar mensaje con una reserva
}
