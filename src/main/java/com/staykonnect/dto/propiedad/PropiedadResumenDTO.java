package com.staykonnect.dto.propiedad;

import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.TipoPropiedad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO resumido para listados de propiedades.
 * Contiene solo la información esencial para mostrar en búsquedas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadResumenDTO {

    private Long id;
    private String titulo;
    private TipoPropiedad tipoPropiedad;
    private String ciudad;
    private String pais;
    private Integer habitaciones;
    private Integer camas;
    private Integer banos;
    private Integer capacidad;
    private BigDecimal precioPorNoche;
    private String imagenPrincipal;
    private EstadoPropiedad estado;
    private Double puntuacionPromedio;
    private Integer totalValoraciones;
    private String anfitrionNombre;
}
