package com.staykonnect.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO para métricas del dashboard de administración
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricasDTO {
    
    // Métricas de usuarios
    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long usuariosBaneados;
    private Long nuevoUsuariosUltimos30Dias;
    private Map<String, Long> usuariosPorRol; // ROL -> count
    
    // Métricas de propiedades
    private Long totalPropiedades;
    private Long propiedadesAprobadas;
    private Long propiedadesPendientes;
    private Long propiedadesRechazadas;
    private Map<String, Long> propiedadesPorTipo; // TIPO -> count
    
    // Métricas de reservas
    private Long totalReservas;
    private Long reservasPendientes;
    private Long reservasConfirmadas;
    private Long reservasCompletadas;
    private Long reservasCanceladas;
    private Map<String, Long> reservasPorEstado; // ESTADO -> count
    
    // Métricas de pagos
    private BigDecimal ingresosTotales;
    private BigDecimal ingresosMesActual;
    private BigDecimal comisionesTotales;
    private BigDecimal comisionesMesActual;
    private Long pagosPendientes;
    private Long pagosCompletados;
    private Long pagosFallidos;
    
    // Métricas de valoraciones
    private Long totalValoraciones;
    private Double puntuacionPromedioGeneral;
    private Long valoracionesPendientesModeracion;
    private Long valoracionesOcultas;
    
    // Métricas de mensajería
    private Long totalMensajes;
    private Long mensajesUltimos30Dias;
    private Long conversacionesActivas;
    
    // Tasas de conversión
    private Double tasaConversionReservas; // (confirmadas / total) * 100
    private Double tasaCompletamiento; // (completadas / confirmadas) * 100
    private Double tasaCancelacion; // (canceladas / total) * 100
    private Double tasaAprobacionPropiedades; // (aprobadas / total) * 100
}
