package com.staykonnect.dto.reserva;

import com.staykonnect.domain.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO completo de reserva con toda la información.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDTO {

    private Long id;
    private EstadoReserva estado;
    
    // Fechas
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    private Integer numeroNoches;
    
    // Huéspedes
    private Integer numeroHuespedes;
    private String notasEspeciales;
    
    // Costos
    private BigDecimal precioTotal;
    private BigDecimal precioNoche;
    private BigDecimal precioLimpieza;
    private BigDecimal depositoSeguridad;
    private BigDecimal comisionPlataforma;
    
    // Propiedad
    private Long propiedadId;
    private String propiedadTitulo;
    private String propiedadDireccion;
    private String propiedadCiudad;
    private String propiedadImagenPrincipal;
    
    // Viajero
    private Long viajeroId;
    private String viajeroNombre;
    private String viajeroEmail;
    private String viajeroTelefono;
    
    // Anfitrión
    private Long anfitrionId;
    private String anfitrionNombre;
    private String anfitrionEmail;
    private String anfitrionTelefono;
    
    // Fechas de sistema
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaCancelacion;
}
