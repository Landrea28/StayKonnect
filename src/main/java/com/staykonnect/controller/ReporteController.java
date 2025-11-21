package com.staykonnect.controller;

import com.staykonnect.dto.reporte.*;
import com.staykonnect.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

/**
 * Controlador REST para reportes avanzados y análisis de datos
 */
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para reportes avanzados y análisis de datos")
@PreAuthorize("hasRole('ADMIN')")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/ingresos")
    @Operation(summary = "Obtener reporte de ingresos por período")
    public ResponseEntity<List<ReporteIngresoDTO>> obtenerReporteIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String periodo) {
        
        List<ReporteIngresoDTO> reporte = reporteService.generarReporteIngresos(fechaInicio, fechaFin, periodo);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/ocupacion")
    @Operation(summary = "Obtener reporte de ocupación por propiedad")
    public ResponseEntity<List<ReporteOcupacionDTO>> obtenerReporteOcupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        List<ReporteOcupacionDTO> reporte = reporteService.generarReporteOcupacion(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/comisiones")
    @Operation(summary = "Obtener reporte de comisiones por período")
    public ResponseEntity<List<ReporteComisionDTO>> obtenerReporteComisiones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String periodo) {
        
        List<ReporteComisionDTO> reporte = reporteService.generarReporteComisiones(fechaInicio, fechaFin, periodo);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/top-propiedades")
    @Operation(summary = "Obtener top propiedades por ingresos")
    public ResponseEntity<List<TopPropiedadDTO>> obtenerTopPropiedades(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        List<TopPropiedadDTO> top = reporteService.obtenerTopPropiedades(fechaInicio, fechaFin, limite);
        return ResponseEntity.ok(top);
    }

    @GetMapping("/top-anfitriones")
    @Operation(summary = "Obtener top anfitriones por ingresos")
    public ResponseEntity<List<TopAnfitrionDTO>> obtenerTopAnfitriones(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        List<TopAnfitrionDTO> top = reporteService.obtenerTopAnfitriones(fechaInicio, fechaFin, limite);
        return ResponseEntity.ok(top);
    }

    @GetMapping("/estacionalidad")
    @Operation(summary = "Obtener análisis de estacionalidad por mes")
    public ResponseEntity<List<EstacionalidadDTO>> obtenerEstacionalidad(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int anio) {
        
        List<EstacionalidadDTO> estacionalidad = reporteService.analizarEstacionalidad(anio);
        return ResponseEntity.ok(estacionalidad);
    }

    @GetMapping("/grafico/ingresos")
    @Operation(summary = "Obtener datos para gráfico de ingresos")
    public ResponseEntity<DatosGraficoDTO> obtenerGraficoIngresos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String periodo) {
        
        List<ReporteIngresoDTO> reporte = reporteService.generarReporteIngresos(fechaInicio, fechaFin, periodo);
        DatosGraficoDTO grafico = reporteService.generarGraficoIngresos(reporte);
        return ResponseEntity.ok(grafico);
    }

    @GetMapping("/grafico/ocupacion")
    @Operation(summary = "Obtener datos para gráfico de ocupación")
    public ResponseEntity<DatosGraficoDTO> obtenerGraficoOcupacion(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        
        List<ReporteOcupacionDTO> reporte = reporteService.generarReporteOcupacion(fechaInicio, fechaFin);
        DatosGraficoDTO grafico = reporteService.generarGraficoOcupacion(reporte);
        return ResponseEntity.ok(grafico);
    }

    @GetMapping("/grafico/estacionalidad")
    @Operation(summary = "Obtener datos para gráfico de estacionalidad")
    public ResponseEntity<DatosGraficoDTO> obtenerGraficoEstacionalidad(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int anio) {
        
        List<EstacionalidadDTO> estacionalidad = reporteService.analizarEstacionalidad(anio);
        DatosGraficoDTO grafico = reporteService.generarGraficoEstacionalidad(estacionalidad);
        return ResponseEntity.ok(grafico);
    }

    @GetMapping("/exportar/ingresos/pdf")
    @Operation(summary = "Exportar reporte de ingresos a PDF")
    public ResponseEntity<byte[]> exportarIngresosPDF(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String periodo) {
        
        List<ReporteIngresoDTO> reporte = reporteService.generarReporteIngresos(fechaInicio, fechaFin, periodo);
        byte[] pdf = reporteService.exportarIngresosPDF(reporte, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-ingresos.pdf");
        
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/exportar/ingresos/excel")
    @Operation(summary = "Exportar reporte de ingresos a Excel")
    public ResponseEntity<byte[]> exportarIngresosExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "MENSUAL") String periodo) {
        
        List<ReporteIngresoDTO> reporte = reporteService.generarReporteIngresos(fechaInicio, fechaFin, periodo);
        byte[] excel = reporteService.exportarIngresosExcel(reporte, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "reporte-ingresos.xlsx");
        
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }
}
