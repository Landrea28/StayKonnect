package com.staykonnect.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    private String tipoReporte; // "INGRESOS", "OCUPACION", "COMISIONES", "TOP_PROPIEDADES", "TOP_ANFITRIONES", "ESTACIONALIDAD"
    private String formato; // "PDF", "EXCEL"
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String periodo; // "DIARIO", "SEMANAL", "MENSUAL", "TRIMESTRAL", "ANUAL"
    private Integer limite; // Para tops
    private String ordenarPor; // Campo de ordenamiento
}
