package com.staykonnect.dto.valoracion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponderValoracionRequest {

    @NotBlank(message = "La respuesta no puede estar vacía")
    @Size(min = 10, max = 1000, message = "La respuesta debe tener entre 10 y 1000 caracteres")
    private String respuesta;
}
