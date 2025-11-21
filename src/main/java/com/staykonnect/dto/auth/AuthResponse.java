package com.staykonnect.dto.auth;

import com.staykonnect.domain.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de autenticación exitosa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long userId;
    private String email;
    private String nombreCompleto;
    private RolUsuario rol;
    private boolean emailVerificado;
}
