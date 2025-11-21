package com.staykonnect.dto.propiedad;

import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.TipoPropiedad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la respuesta de información de propiedad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private TipoPropiedad tipoPropiedad;
    
    // Ubicación
    private String direccion;
    private String ciudad;
    private String pais;
    private String codigoPostal;
    private Double latitud;
    private Double longitud;
    
    // Características
    private Integer habitaciones;
    private Integer camas;
    private Integer banos;
    private Integer capacidad;
    private Double areaM2;
    
    // Precios
    private BigDecimal precioPorNoche;
    private BigDecimal precioLimpieza;
    private BigDecimal depositoSeguridad;
    
    // Servicios e imágenes
    private List<String> servicios;
    private List<String> imagenes;
    
    // Reglas y horarios
    private String reglasCasa;
    private LocalTime horaCheckin;
    private LocalTime horaCheckout;
    private Integer estanciaMinima;
    private Integer estanciaMaxima;
    
    // Estado y métricas
    private EstadoPropiedad estado;
    private Double puntuacionPromedio;
    private Integer totalValoraciones;
    private Integer totalReservas;
    private Boolean verificacionCompleta;
    
    // Anfitrión
    private Long anfitrionId;
    private String anfitrionNombre;
    private String anfitrionEmail;
    
    // Auditoría
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
