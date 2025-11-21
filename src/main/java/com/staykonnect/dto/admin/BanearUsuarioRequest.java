package com.staykonnect.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para banear un usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanearUsuarioRequest {
    
    @NotBlank(message = "La razón del baneo es obligatoria")
    @Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
    private String razon;
}
