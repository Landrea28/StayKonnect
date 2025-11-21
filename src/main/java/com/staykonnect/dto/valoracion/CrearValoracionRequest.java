package com.staykonnect.dto.valoracion;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearValoracionRequest {

    @NotNull(message = "La puntuación es obligatoria")
    @Min(value = 1, message = "La puntuación mínima es 1")
    @Max(value = 5, message = "La puntuación máxima es 5")
    private Integer puntuacion;

    @Size(min = 10, max = 2000, message = "El comentario debe tener entre 10 y 2000 caracteres")
    private String comentario; // Opcional pero recomendado
}
