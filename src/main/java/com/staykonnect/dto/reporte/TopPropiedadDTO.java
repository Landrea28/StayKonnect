package com.staykonnect.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopPropiedadDTO {
    private Long propiedadId;
    private String nombre;
    private String ciudad;
    private String tipo;
    private Long anfitrionId;
    private String anfitrionNombre;
    private BigDecimal ingresosGenerados;
    private Integer numeroReservas;
    private BigDecimal tasaOcupacion;
    private BigDecimal puntuacionPromedio;
    private Integer numeroValoraciones;
    private BigDecimal precioPromedioPorNoche;
    private Integer posicion; // Ranking position
}
