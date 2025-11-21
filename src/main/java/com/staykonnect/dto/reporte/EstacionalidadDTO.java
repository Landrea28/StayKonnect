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
public class EstacionalidadDTO {
    private Integer mes; // 1-12
    private String nombreMes;
    private String temporada; // "ALTA", "MEDIA", "BAJA"
    private Integer numeroReservas;
    private BigDecimal ingresosGenerados;
    private BigDecimal tasaOcupacionPromedio;
    private BigDecimal precioPromedioPorNoche;
    private Integer numeroViajeros;
    private BigDecimal tasaCrecimientoAnual; // Comparado con mismo mes año anterior
}
