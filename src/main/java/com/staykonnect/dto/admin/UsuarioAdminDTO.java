package com.staykonnect.dto.admin;

import com.staykonnect.domain.entity.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO extendido de usuario para panel de administración
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAdminDTO {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Rol rol;
    private Boolean emailVerificado;
    private Boolean activo;
    private Boolean baneado;
    private LocalDateTime fechaBaneo;
    private String razonBaneo;
    private String baneadoPorNombre;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    // Estadísticas del usuario
    private Long totalPropiedades;
    private Long totalReservasComoViajero;
    private Long totalReservasComoAnfitrion;
    private Long totalValoracionesRecibidas;
    private Double puntuacionPromedio;
    private Long totalMensajesEnviados;
}
