package com.staykonnect.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatosGraficoDTO {
    private String titulo;
    private String tipo; // "line", "bar", "pie", "area"
    private List<String> etiquetas; // Labels para eje X
    private List<SerieGraficoDTO> series;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SerieGraficoDTO {
        private String nombre;
        private String color;
        private List<BigDecimal> datos;
        private String tipo; // Para gráficos mixtos
    }
}
