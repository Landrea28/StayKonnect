package com.staykonnect.dto.admin;

import com.staykonnect.domain.entity.enums.EstadoPropiedad;
import com.staykonnect.domain.entity.enums.TipoPropiedad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO extendido de propiedad para panel de administración
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadAdminDTO {
    
    private Long id;
    private String titulo;
    private String descripcion;
    private TipoPropiedad tipo;
    private EstadoPropiedad estado;
    private BigDecimal precioPorNoche;
    private String ciudad;
    private String pais;
    private Integer capacidadMaxima;
    private Boolean aprobada;
    private LocalDateTime fechaAprobacion;
    private String aprobadaPorNombre;
    private Boolean rechazada;
    private LocalDateTime fechaRechazo;
    private String razonRechazo;
    private String rechazadaPorNombre;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    // Información del anfitrión
    private Long anfitrionId;
    private String anfitrionNombre;
    private String anfitrionEmail;
    
    // Estadísticas de la propiedad
    private Long totalReservas;
    private Long reservasCompletadas;
    private Long totalValoraciones;
    private Double puntuacionPromedio;
    private BigDecimal ingresosGenerados;
}
