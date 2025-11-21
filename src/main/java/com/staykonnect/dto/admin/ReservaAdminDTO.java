package com.staykonnect.dto.admin;

import com.staykonnect.domain.entity.enums.EstadoReserva;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO extendido de reserva para panel de administración
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaAdminDTO {
    
    private Long id;
    private EstadoReserva estado;
    private LocalDateTime fechaCheckin;
    private LocalDateTime fechaCheckout;
    private Integer numeroHuespedes;
    private BigDecimal precioTotalNoche;
    private BigDecimal costoLimpieza;
    private BigDecimal comisionPlataforma;
    private BigDecimal precioTotal;
    private String codigoPago;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    // Información del viajero
    private Long viajeroId;
    private String viajeroNombre;
    private String viajeroEmail;
    private String viajeroTelefono;
    
    // Información del anfitrión
    private Long anfitrionId;
    private String anfitrionNombre;
    private String anfitrionEmail;
    
    // Información de la propiedad
    private Long propiedadId;
    private String propiedadTitulo;
    private String propiedadCiudad;
    private String propiedadPais;
    
    // Información de pago
    private String pagoEstado;
    private String pagoMetodo;
    
    // Información de valoración
    private Boolean tieneValoracion;
    private Integer valoracionPuntuacion;
}
