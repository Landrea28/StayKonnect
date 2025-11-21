package com.staykonnect.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.staykonnect.domain.repository.ReservaRepository;
import com.staykonnect.dto.reporte.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para generación de reportes avanzados y análisis de datos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReporteService {

    private final ReservaRepository reservaRepository;
    
    private static final String[] NOMBRES_MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    /**
     * Genera reporte de ingresos por período
     */
    public List<ReporteIngresoDTO> generarReporteIngresos(LocalDateTime fechaInicio, LocalDateTime fechaFin, String periodo) {
        log.info("Generando reporte de ingresos desde {} hasta {} con período {}", fechaInicio, fechaFin, periodo);
        
        LocalDate inicio = fechaInicio.toLocalDate();
        LocalDate fin = fechaFin.toLocalDate();
        String periodoPg = mapearPeriodo(periodo);
        
        List<Object[]> resultados = reservaRepository.obtenerIngresosPorPeriodo(periodoPg, inicio, fin);
        
        List<ReporteIngresoDTO> reportes = new ArrayList<>();
        ReporteIngresoDTO anterior = null;
        
        for (int i = 0; i < resultados.size(); i++) {
            Object[] row = resultados.get(i);
            ReporteIngresoDTO dto = ReporteIngresoDTO.builder()
                    .fecha(((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate())
                    .periodo(formatearPeriodo(((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate(), periodo))
                    .numeroReservas(((Number) row[1]).intValue())
                    .ingresosBrutos(row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO)
                    .comisiones(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO)
                    .ingresosNetos(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                    .build();
            
            // Calcular métricas adicionales
            if (dto.getNumeroReservas() > 0) {
                dto.setIngresoPorReserva(dto.getIngresosBrutos().divide(
                    BigDecimal.valueOf(dto.getNumeroReservas()), 2, RoundingMode.HALF_UP));
            } else {
                dto.setIngresoPorReserva(BigDecimal.ZERO);
            }
            
            // Calcular tasa de crecimiento vs período anterior
            if (anterior != null && anterior.getIngresosBrutos().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal crecimiento = dto.getIngresosBrutos()
                    .subtract(anterior.getIngresosBrutos())
                    .divide(anterior.getIngresosBrutos(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                dto.setTasaCrecimiento(crecimiento);
            } else {
                dto.setTasaCrecimiento(BigDecimal.ZERO);
            }
            
            reportes.add(dto);
            anterior = dto;
        }
        
        return reportes;
    }

    /**
     * Genera reporte de ocupación por propiedad
     */
    public List<ReporteOcupacionDTO> generarReporteOcupacion(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        log.info("Generando reporte de ocupación desde {} hasta {}", fechaInicio, fechaFin);
        
        LocalDate inicio = fechaInicio.toLocalDate();
        LocalDate fin = fechaFin.toLocalDate();
        long diasPeriodo = ChronoUnit.DAYS.between(inicio, fin);
        
        List<Object[]> resultados = reservaRepository.obtenerOcupacionPorPropiedad(inicio, fin);
        
        return resultados.stream().map(row -> {
            int diasReservados = row[7] != null ? ((Number) row[7]).intValue() : 0;
            BigDecimal tasaOcupacion = diasPeriodo > 0 
                ? BigDecimal.valueOf(diasReservados)
                    .divide(BigDecimal.valueOf(diasPeriodo), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            BigDecimal ingresos = row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO;
            BigDecimal ingresoPorDia = diasReservados > 0
                ? ingresos.divide(BigDecimal.valueOf(diasReservados), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            return ReporteOcupacionDTO.builder()
                    .propiedadId(((Number) row[0]).longValue())
                    .propiedadNombre((String) row[1])
                    .propiedadCiudad((String) row[2])
                    .propiedadTipo((String) row[3])
                    .numeroReservas(row[4] != null ? ((Number) row[4]).intValue() : 0)
                    .ingresosGenerados(ingresos)
                    .puntuacionPromedio(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .diasReservados(diasReservados)
                    .diasDisponibles((int) diasPeriodo)
                    .diasBloqueados(0) // Podría calcularse si hay un sistema de bloqueos
                    .tasaOcupacion(tasaOcupacion)
                    .ingresoPorDiaReservado(ingresoPorDia)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Genera reporte de comisiones por período
     */
    public List<ReporteComisionDTO> generarReporteComisiones(LocalDateTime fechaInicio, LocalDateTime fechaFin, String periodo) {
        log.info("Generando reporte de comisiones desde {} hasta {} con período {}", fechaInicio, fechaFin, periodo);
        
        LocalDate inicio = fechaInicio.toLocalDate();
        LocalDate fin = fechaFin.toLocalDate();
        String periodoPg = mapearPeriodo(periodo);
        
        List<Object[]> resultados = reservaRepository.obtenerComisionesPorPeriodo(periodoPg, inicio, fin);
        
        return resultados.stream().map(row -> {
            BigDecimal comisiones = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            BigDecimal ingresosTotales = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            BigDecimal comisionPromedio = row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO;
            
            BigDecimal porcentajeComision = ingresosTotales.compareTo(BigDecimal.ZERO) > 0
                ? comisiones.divide(ingresosTotales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            return ReporteComisionDTO.builder()
                    .fecha(((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate())
                    .periodo(formatearPeriodo(((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate(), periodo))
                    .numeroTransacciones(((Number) row[1]).intValue())
                    .comisionesGeneradas(comisiones)
                    .comisionesReales(comisiones) // Asumimos que todas están pagadas
                    .comisionesPendientes(BigDecimal.ZERO)
                    .comisionPromedio(comisionPromedio)
                    .porcentajeComisionPromedio(porcentajeComision)
                    .ingresosTotales(ingresosTotales)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Obtiene el top de propiedades por ingresos
     */
    public List<TopPropiedadDTO> obtenerTopPropiedades(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite) {
        log.info("Obteniendo top {} propiedades desde {} hasta {}", limite, fechaInicio, fechaFin);
        
        LocalDate inicio = fechaInicio.toLocalDate();
        LocalDate fin = fechaFin.toLocalDate();
        
        List<Object[]> resultados = reservaRepository.obtenerTopPropiedadesPorIngresos(inicio, fin, limite);
        
        List<TopPropiedadDTO> tops = new ArrayList<>();
        for (int i = 0; i < resultados.size(); i++) {
            Object[] row = resultados.get(i);
            
            int diasReservados = row[6] != null ? ((Number) row[6]).intValue() : 0;
            long diasPeriodo = ChronoUnit.DAYS.between(inicio, fin);
            BigDecimal tasaOcupacion = diasPeriodo > 0
                ? BigDecimal.valueOf(diasReservados)
                    .divide(BigDecimal.valueOf(diasPeriodo), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            TopPropiedadDTO dto = TopPropiedadDTO.builder()
                    .propiedadId(((Number) row[0]).longValue())
                    .nombre((String) row[1])
                    .ciudad((String) row[2])
                    .tipo((String) row[3])
                    .anfitrionId(((Number) row[4]).longValue())
                    .anfitrionNombre((String) row[5])
                    .numeroReservas(row[6] != null ? ((Number) row[6]).intValue() : 0)
                    .ingresosGenerados(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .puntuacionPromedio(row[8] != null ? new BigDecimal(row[8].toString()) : BigDecimal.ZERO)
                    .numeroValoraciones(row[9] != null ? ((Number) row[9]).intValue() : 0)
                    .precioPromedioPorNoche(row[10] != null ? new BigDecimal(row[10].toString()) : BigDecimal.ZERO)
                    .tasaOcupacion(tasaOcupacion)
                    .posicion(i + 1)
                    .build();
            
            tops.add(dto);
        }
        
        return tops;
    }

    /**
     * Obtiene el top de anfitriones
     */
    public List<TopAnfitrionDTO> obtenerTopAnfitriones(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite) {
        log.info("Obteniendo top {} anfitriones desde {} hasta {}", limite, fechaInicio, fechaFin);
        
        LocalDate inicio = fechaInicio.toLocalDate();
        LocalDate fin = fechaFin.toLocalDate();
        
        List<Object[]> resultados = reservaRepository.obtenerTopAnfitriones(inicio, fin, limite);
        
        List<TopAnfitrionDTO> tops = new ArrayList<>();
        for (int i = 0; i < resultados.size(); i++) {
            Object[] row = resultados.get(i);
            
            int totalReservas = row[5] != null ? ((Number) row[5]).intValue() : 0;
            int reservasCompletadas = row[6] != null ? ((Number) row[6]).intValue() : 0;
            BigDecimal tasaCompletamiento = totalReservas > 0
                ? BigDecimal.valueOf(reservasCompletadas)
                    .divide(BigDecimal.valueOf(totalReservas), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
            
            TopAnfitrionDTO dto = TopAnfitrionDTO.builder()
                    .anfitrionId(((Number) row[0]).longValue())
                    .nombre((String) row[1])
                    .email((String) row[2])
                    .numeroPropiedades(row[3] != null ? ((Number) row[3]).intValue() : 0)
                    .propiedadesActivas(row[4] != null ? ((Number) row[4]).intValue() : 0)
                    .numeroReservasTotales(totalReservas)
                    .numeroReservasCompletadas(reservasCompletadas)
                    .ingresosGenerados(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .puntuacionPromedio(row[8] != null ? new BigDecimal(row[8].toString()) : BigDecimal.ZERO)
                    .numeroValoracionesRecibidas(row[9] != null ? ((Number) row[9]).intValue() : 0)
                    .tasaCompletamiento(tasaCompletamiento)
                    .posicion(i + 1)
                    .build();
            
            tops.add(dto);
        }
        
        return tops;
    }

    /**
     * Analiza la estacionalidad por mes
     */
    public List<EstacionalidadDTO> analizarEstacionalidad(int anio) {
        log.info("Analizando estacionalidad para el año {}", anio);
        
        List<Object[]> resultados = reservaRepository.obtenerEstacionalidadPorMes(anio);
        
        // Calcular estadísticas para determinar temporada
        double promedioReservas = resultados.stream()
            .mapToDouble(row -> ((Number) row[1]).doubleValue())
            .average()
            .orElse(0.0);
        
        double promedioIngresos = resultados.stream()
            .mapToDouble(row -> row[2] != null ? ((Number) row[2]).doubleValue() : 0.0)
            .average()
            .orElse(0.0);
        
        return resultados.stream().map(row -> {
            int mes = ((Number) row[0]).intValue();
            int numeroReservas = row[1] != null ? ((Number) row[1]).intValue() : 0;
            BigDecimal ingresos = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            
            // Determinar temporada basada en reservas e ingresos
            String temporada;
            if (numeroReservas >= promedioReservas * 1.2 && ingresos.doubleValue() >= promedioIngresos * 1.2) {
                temporada = "ALTA";
            } else if (numeroReservas <= promedioReservas * 0.8 || ingresos.doubleValue() <= promedioIngresos * 0.8) {
                temporada = "BAJA";
            } else {
                temporada = "MEDIA";
            }
            
            // Calcular tasa de ocupación promedio (simplificado)
            long diasMes = LocalDate.of(anio, mes, 1).lengthOfMonth();
            BigDecimal tasaOcupacion = BigDecimal.valueOf(numeroReservas * 7) // Asumimos 7 días promedio por reserva
                .divide(BigDecimal.valueOf(diasMes * 30), 4, RoundingMode.HALF_UP) // Asumimos 30 propiedades promedio
                .multiply(BigDecimal.valueOf(100));
            
            return EstacionalidadDTO.builder()
                    .mes(mes)
                    .nombreMes(NOMBRES_MESES[mes - 1])
                    .temporada(temporada)
                    .numeroReservas(numeroReservas)
                    .ingresosGenerados(ingresos)
                    .tasaOcupacionPromedio(tasaOcupacion)
                    .precioPromedioPorNoche(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO)
                    .numeroViajeros(row[4] != null ? ((Number) row[4]).intValue() : 0)
                    .tasaCrecimientoAnual(BigDecimal.ZERO) // Requiere comparación con año anterior
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Genera datos para gráficos de ingresos
     */
    public DatosGraficoDTO generarGraficoIngresos(List<ReporteIngresoDTO> reportes) {
        List<String> etiquetas = reportes.stream()
            .map(ReporteIngresoDTO::getPeriodo)
            .collect(Collectors.toList());
        
        List<BigDecimal> ingresosBrutos = reportes.stream()
            .map(ReporteIngresoDTO::getIngresosBrutos)
            .collect(Collectors.toList());
        
        List<BigDecimal> comisiones = reportes.stream()
            .map(ReporteIngresoDTO::getComisiones)
            .collect(Collectors.toList());
        
        List<BigDecimal> ingresosNetos = reportes.stream()
            .map(ReporteIngresoDTO::getIngresosNetos)
            .collect(Collectors.toList());
        
        return DatosGraficoDTO.builder()
            .titulo("Análisis de Ingresos")
            .tipo("line")
            .etiquetas(etiquetas)
            .series(Arrays.asList(
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Ingresos Brutos")
                    .color("#10b981")
                    .datos(ingresosBrutos)
                    .build(),
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Comisiones")
                    .color("#f59e0b")
                    .datos(comisiones)
                    .build(),
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Ingresos Netos")
                    .color("#3b82f6")
                    .datos(ingresosNetos)
                    .build()
            ))
            .build();
    }

    /**
     * Genera datos para gráfico de ocupación
     */
    public DatosGraficoDTO generarGraficoOcupacion(List<ReporteOcupacionDTO> reportes) {
        // Top 10 propiedades por tasa de ocupación
        List<ReporteOcupacionDTO> top10 = reportes.stream()
            .sorted((a, b) -> b.getTasaOcupacion().compareTo(a.getTasaOcupacion()))
            .limit(10)
            .collect(Collectors.toList());
        
        List<String> etiquetas = top10.stream()
            .map(r -> r.getPropiedadNombre().length() > 20 
                ? r.getPropiedadNombre().substring(0, 20) + "..." 
                : r.getPropiedadNombre())
            .collect(Collectors.toList());
        
        List<BigDecimal> tasasOcupacion = top10.stream()
            .map(ReporteOcupacionDTO::getTasaOcupacion)
            .collect(Collectors.toList());
        
        return DatosGraficoDTO.builder()
            .titulo("Top 10 Propiedades por Ocupación")
            .tipo("bar")
            .etiquetas(etiquetas)
            .series(Arrays.asList(
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Tasa de Ocupación (%)")
                    .color("#8b5cf6")
                    .datos(tasasOcupacion)
                    .build()
            ))
            .build();
    }

    /**
     * Genera datos para gráfico de estacionalidad
     */
    public DatosGraficoDTO generarGraficoEstacionalidad(List<EstacionalidadDTO> estacionalidad) {
        List<String> etiquetas = estacionalidad.stream()
            .map(EstacionalidadDTO::getNombreMes)
            .collect(Collectors.toList());
        
        List<BigDecimal> reservas = estacionalidad.stream()
            .map(e -> BigDecimal.valueOf(e.getNumeroReservas()))
            .collect(Collectors.toList());
        
        List<BigDecimal> ingresos = estacionalidad.stream()
            .map(e -> e.getIngresosGenerados().divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP))
            .collect(Collectors.toList());
        
        return DatosGraficoDTO.builder()
            .titulo("Estacionalidad - Reservas e Ingresos por Mes")
            .tipo("line")
            .etiquetas(etiquetas)
            .series(Arrays.asList(
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Número de Reservas")
                    .color("#ec4899")
                    .datos(reservas)
                    .tipo("bar")
                    .build(),
                DatosGraficoDTO.SerieGraficoDTO.builder()
                    .nombre("Ingresos (miles)")
                    .color("#06b6d4")
                    .datos(ingresos)
                    .tipo("line")
                    .build()
            ))
            .build();
    }

    /**
     * Exporta reporte de ingresos a PDF
     */
    public byte[] exportarIngresosPDF(List<ReporteIngresoDTO> reportes, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título
            document.add(new Paragraph("Reporte de Ingresos")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph(String.format("Período: %s - %s", 
                fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n"));
            
            // Tabla
            float[] columnWidths = {3, 2, 2, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            
            // Headers
            table.addHeaderCell("Período");
            table.addHeaderCell("Reservas");
            table.addHeaderCell("Ingresos Brutos");
            table.addHeaderCell("Comisiones");
            table.addHeaderCell("Ingresos Netos");
            table.addHeaderCell("Promedio/Reserva");
            
            // Datos
            BigDecimal totalBrutos = BigDecimal.ZERO;
            BigDecimal totalComisiones = BigDecimal.ZERO;
            BigDecimal totalNetos = BigDecimal.ZERO;
            int totalReservas = 0;
            
            for (ReporteIngresoDTO dto : reportes) {
                table.addCell(dto.getPeriodo());
                table.addCell(String.valueOf(dto.getNumeroReservas()));
                table.addCell("$" + dto.getIngresosBrutos().setScale(2, RoundingMode.HALF_UP));
                table.addCell("$" + dto.getComisiones().setScale(2, RoundingMode.HALF_UP));
                table.addCell("$" + dto.getIngresosNetos().setScale(2, RoundingMode.HALF_UP));
                table.addCell("$" + dto.getIngresoPorReserva().setScale(2, RoundingMode.HALF_UP));
                
                totalBrutos = totalBrutos.add(dto.getIngresosBrutos());
                totalComisiones = totalComisiones.add(dto.getComisiones());
                totalNetos = totalNetos.add(dto.getIngresosNetos());
                totalReservas += dto.getNumeroReservas();
            }
            
            // Totales
            table.addCell("TOTAL").setBold();
            table.addCell(String.valueOf(totalReservas)).setBold();
            table.addCell("$" + totalBrutos.setScale(2, RoundingMode.HALF_UP)).setBold();
            table.addCell("$" + totalComisiones.setScale(2, RoundingMode.HALF_UP)).setBold();
            table.addCell("$" + totalNetos.setScale(2, RoundingMode.HALF_UP)).setBold();
            BigDecimal promedio = totalReservas > 0 
                ? totalBrutos.divide(BigDecimal.valueOf(totalReservas), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            table.addCell("$" + promedio).setBold();
            
            document.add(table);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar PDF de ingresos", e);
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    /**
     * Exporta reporte de ingresos a Excel
     */
    public byte[] exportarIngresosExcel(List<ReporteIngresoDTO> reportes, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte de Ingresos");
            
            // Estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));
            
            // Título
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Reporte de Ingresos");
            titleCell.setCellStyle(headerStyle);
            
            Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue(String.format("Período: %s - %s",
                fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            
            // Headers
            Row headerRow = sheet.createRow(3);
            String[] headers = {"Período", "Reservas", "Ingresos Brutos", "Comisiones", 
                               "Ingresos Netos", "Promedio/Reserva", "Crecimiento %"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            int rowNum = 4;
            BigDecimal totalBrutos = BigDecimal.ZERO;
            BigDecimal totalComisiones = BigDecimal.ZERO;
            BigDecimal totalNetos = BigDecimal.ZERO;
            int totalReservas = 0;
            
            for (ReporteIngresoDTO dto : reportes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getPeriodo());
                row.createCell(1).setCellValue(dto.getNumeroReservas());
                
                Cell brutosCell = row.createCell(2);
                brutosCell.setCellValue(dto.getIngresosBrutos().doubleValue());
                brutosCell.setCellStyle(currencyStyle);
                
                Cell comisionesCell = row.createCell(3);
                comisionesCell.setCellValue(dto.getComisiones().doubleValue());
                comisionesCell.setCellStyle(currencyStyle);
                
                Cell netosCell = row.createCell(4);
                netosCell.setCellValue(dto.getIngresosNetos().doubleValue());
                netosCell.setCellStyle(currencyStyle);
                
                Cell promedioCell = row.createCell(5);
                promedioCell.setCellValue(dto.getIngresoPorReserva().doubleValue());
                promedioCell.setCellStyle(currencyStyle);
                
                row.createCell(6).setCellValue(dto.getTasaCrecimiento().doubleValue());
                
                totalBrutos = totalBrutos.add(dto.getIngresosBrutos());
                totalComisiones = totalComisiones.add(dto.getComisiones());
                totalNetos = totalNetos.add(dto.getIngresosNetos());
                totalReservas += dto.getNumeroReservas();
            }
            
            // Totales
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL");
            totalLabelCell.setCellStyle(headerStyle);
            
            Cell totalReservasCell = totalRow.createCell(1);
            totalReservasCell.setCellValue(totalReservas);
            totalReservasCell.setCellStyle(headerStyle);
            
            Cell totalBrutosCell = totalRow.createCell(2);
            totalBrutosCell.setCellValue(totalBrutos.doubleValue());
            totalBrutosCell.setCellStyle(currencyStyle);
            
            Cell totalComisionesCell = totalRow.createCell(3);
            totalComisionesCell.setCellValue(totalComisiones.doubleValue());
            totalComisionesCell.setCellStyle(currencyStyle);
            
            Cell totalNetosCell = totalRow.createCell(4);
            totalNetosCell.setCellValue(totalNetos.doubleValue());
            totalNetosCell.setCellStyle(currencyStyle);
            
            // Auto-size columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al exportar Excel de ingresos", e);
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    // Métodos auxiliares
    
    private String mapearPeriodo(String periodo) {
        return switch (periodo.toUpperCase()) {
            case "DIARIO", "DAY" -> "day";
            case "SEMANAL", "WEEK" -> "week";
            case "MENSUAL", "MONTH" -> "month";
            case "TRIMESTRAL", "QUARTER" -> "quarter";
            case "ANUAL", "YEAR" -> "year";
            default -> "month";
        };
    }
    
    private String formatearPeriodo(LocalDate fecha, String periodo) {
        return switch (periodo.toUpperCase()) {
            case "DIARIO", "DAY" -> fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "SEMANAL", "WEEK" -> String.format("Semana %d - %s", 
                fecha.getDayOfYear() / 7 + 1, 
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            case "MENSUAL", "MONTH" -> fecha.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            case "TRIMESTRAL", "QUARTER" -> String.format("Q%d %d", 
                (fecha.getMonthValue() - 1) / 3 + 1, 
                fecha.getYear());
            case "ANUAL", "YEAR" -> String.valueOf(fecha.getYear());
            default -> fecha.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        };
    }
}
