package com.staykonnect.dto.reserva;

import com.staykonnect.domain.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO resumido de reserva para listados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResumenDTO {

    private Long id;
    private EstadoReserva estado;
    
    private LocalDate fechaCheckin;
    private LocalDate fechaCheckout;
    private Integer numeroNoches;
    private Integer numeroHuespedes;
    
    private BigDecimal precioTotal;
    
    // Datos de la propiedad
    private Long propiedadId;
    private String propiedadTitulo;
    private String propiedadCiudad;
    private String propiedadImagenPrincipal;
    
    // Datos del otro usuario (viajero si es anfitrión, anfitrión si es viajero)
    private String contraparteNombre;
    private String contraparteEmail;
}
