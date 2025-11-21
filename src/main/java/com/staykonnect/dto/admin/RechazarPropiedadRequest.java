package com.staykonnect.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para rechazar una propiedad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechazarPropiedadRequest {
    
    @NotBlank(message = "La razón del rechazo es obligatoria")
    @Size(min = 10, max = 1000, message = "La razón debe tener entre 10 y 1000 caracteres")
    private String razon;
}
