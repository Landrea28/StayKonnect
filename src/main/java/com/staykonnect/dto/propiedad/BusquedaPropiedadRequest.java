package com.staykonnect.dto.propiedad;

import com.staykonnect.domain.enums.TipoPropiedad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para los parámetros de búsqueda de propiedades.
 * Todos los campos son opcionales para permitir búsquedas flexibles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaPropiedadRequest {

    // Búsqueda por texto
    private String query; // Busca en título y descripción

    // Ubicación
    private String ciudad;
    private String pais;

    // Fechas de disponibilidad
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Tipo de propiedad
    private TipoPropiedad tipoPropiedad;

    // Capacidad
    private Integer capacidadMinima;
    
    // Características
    private Integer habitacionesMinimas;
    private Integer camasMinimas;
    private Integer banosMinimos;

    // Rango de precio (por noche)
    private BigDecimal precioMinimo;
    private BigDecimal precioMaximo;

    // Servicios requeridos (debe tener TODOS los servicios listados)
    private List<String> servicios;

    // Puntuación mínima
    private Double puntuacionMinima;

    // Ordenamiento
    private String ordenarPor; // precio, puntuacion, reciente
    private String direccion; // asc, desc
}
