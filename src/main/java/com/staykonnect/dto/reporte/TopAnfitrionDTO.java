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
public class TopAnfitrionDTO {
    private Long anfitrionId;
    private String nombre;
    private String email;
    private Integer numeroPropiedades;
    private Integer propiedadesActivas;
    private BigDecimal ingresosGenerados;
    private Integer numeroReservasTotales;
    private Integer numeroReservasCompletadas;
    private BigDecimal tasaCompletamiento;
    private BigDecimal puntuacionPromedio;
    private Integer numeroValoracionesRecibidas;
    private Integer posicion; // Ranking position
}
