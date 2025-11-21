package com.staykonnect.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIngresoDTO {
    private LocalDate fecha;
    private String periodo; // "2024-01", "2024-01-15", "2024-Q1"
    private BigDecimal ingresosBrutos;
    private BigDecimal comisiones;
    private BigDecimal ingresosNetos;
    private Integer numeroReservas;
    private Integer numeroReservasCompletadas;
    private Integer numeroReservasCanceladas;
    private BigDecimal ingresoPorReserva; // Promedio
    private BigDecimal tasaCrecimiento; // Comparado con periodo anterior
}
