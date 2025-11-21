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
public class ReporteOcupacionDTO {
    private Long propiedadId;
    private String propiedadNombre;
    private String propiedadCiudad;
    private String propiedadTipo;
    private Integer diasDisponibles;
    private Integer diasReservados;
    private Integer diasBloqueados;
    private BigDecimal tasaOcupacion; // Porcentaje
    private Integer numeroReservas;
    private BigDecimal ingresosGenerados;
    private BigDecimal ingresoPorDiaReservado;
    private BigDecimal puntuacionPromedio;
}
