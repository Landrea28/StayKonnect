package com.staykonnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitar recuperación de contraseña.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoverPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;
}
