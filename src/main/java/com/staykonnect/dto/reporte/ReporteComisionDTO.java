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
public class ReporteComisionDTO {
    private LocalDate fecha;
    private String periodo;
    private BigDecimal comisionesGeneradas;
    private BigDecimal comisionesReales; // Pagadas
    private BigDecimal comisionesPendientes;
    private Integer numeroTransacciones;
    private BigDecimal comisionPromedio;
    private BigDecimal porcentajeComisionPromedio;
    private BigDecimal ingresosTotales; // Base para las comisiones
}
