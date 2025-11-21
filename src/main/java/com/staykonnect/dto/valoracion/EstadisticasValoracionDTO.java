package com.staykonnect.dto.valoracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasValoracionDTO {

    private Double promedioGeneral;
    private Long totalValoraciones;
    
    // Distribución por estrellas: {5: 45, 4: 30, 3: 15, 2: 7, 1: 3}
    private Map<Integer, Long> distribucionPuntuaciones;
    
    // Porcentajes calculados
    private Double porcentaje5Estrellas;
    private Double porcentaje4Estrellas;
    private Double porcentaje3Estrellas;
    private Double porcentaje2Estrellas;
    private Double porcentaje1Estrella;
}
