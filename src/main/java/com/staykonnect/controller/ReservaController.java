package com.staykonnect.controller;

import com.staykonnect.common.dto.ApiResponse;
import com.staykonnect.dto.reserva.CancelarReservaRequest;
import com.staykonnect.dto.reserva.CrearReservaRequest;
import com.staykonnect.dto.reserva.ReservaDTO;
import com.staykonnect.dto.reserva.ReservaResumenDTO;
import com.staykonnect.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de reservas.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reservas", description = "Endpoints para gestión de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    /**
     * Crea una nueva reserva (solo viajeros).
     */
    @PostMapping
    @PreAuthorize("hasRole('VIAJERO')")
    @Operation(summary = "Crear reserva", description = "Solicita una nueva reserva para una propiedad")
    public ResponseEntity<ApiResponse<ReservaDTO>> crearReserva(@Valid @RequestBody CrearReservaRequest request) {
        ReservaDTO reserva = reservaService.crearReserva(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reserva creada exitosamente. Pendiente de confirmación del anfitrión.", reserva));
    }

    /**
     * Confirma una reserva pendiente (solo anfitrión).
     */
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('ANFITRION')")
    @Operation(summary = "Confirmar reserva", description = "El anfitrión confirma una reserva pendiente")
    public ResponseEntity<ApiResponse<ReservaDTO>> confirmarReserva(@PathVariable Long id) {
        ReservaDTO reserva = reservaService.confirmarReserva(id);
        return ResponseEntity.ok(ApiResponse.success("Reserva confirmada exitosamente", reserva));
    }

    /**
     * Rechaza una reserva pendiente (solo anfitrión).
     */
    @PutMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ANFITRION')")
    @Operation(summary = "Rechazar reserva", description = "El anfitrión rechaza una reserva pendiente")
    public ResponseEntity<ApiResponse<ReservaDTO>> rechazarReserva(
            @PathVariable Long id,
            @RequestParam String motivo) {
        ReservaDTO reserva = reservaService.rechazarReserva(id, motivo);
        return ResponseEntity.ok(ApiResponse.success("Reserva rechazada", reserva));
    }

    /**
     * Cancela una reserva (viajero o anfitrión).
     */
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Cancelar reserva", description = "Cancela una reserva existente")
    public ResponseEntity<ApiResponse<ReservaDTO>> cancelarReserva(
            @PathVariable Long id,
            @Valid @RequestBody CancelarReservaRequest request) {
        ReservaDTO reserva = reservaService.cancelarReserva(id, request);
        return ResponseEntity.ok(ApiResponse.success("Reserva cancelada exitosamente", reserva));
    }

    /**
     * Obtiene los detalles de una reserva.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION', 'ADMIN')")
    @Operation(summary = "Obtener reserva", description = "Obtiene los detalles completos de una reserva")
    public ResponseEntity<ApiResponse<ReservaDTO>> obtenerReserva(@PathVariable Long id) {
        ReservaDTO reserva = reservaService.obtenerReserva(id);
        return ResponseEntity.ok(ApiResponse.success("Reserva obtenida exitosamente", reserva));
    }

    /**
     * Lista las reservas del usuario como viajero.
     */
    @GetMapping("/mis-reservas")
    @PreAuthorize("hasRole('VIAJERO')")
    @Operation(summary = "Mis reservas como viajero", description = "Lista todas las reservas realizadas por el usuario")
    public ResponseEntity<ApiResponse<Page<ReservaResumenDTO>>> listarMisReservas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservaResumenDTO> reservas = reservaService.listarMisReservasComoViajero(pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas exitosamente", reservas));
    }

    /**
     * Lista las reservas de las propiedades del anfitrión.
     */
    @GetMapping("/recibidas")
    @PreAuthorize("hasRole('ANFITRION')")
    @Operation(summary = "Reservas recibidas", description = "Lista las reservas de las propiedades del anfitrión")
    public ResponseEntity<ApiResponse<Page<ReservaResumenDTO>>> listarReservasRecibidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservaResumenDTO> reservas = reservaService.listarReservasComoAnfitrion(pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas exitosamente", reservas));
    }

    /**
     * Lista las reservas de una propiedad específica (solo anfitrión o admin).
     */
    @GetMapping("/propiedad/{propiedadId}")
    @PreAuthorize("hasAnyRole('ANFITRION', 'ADMIN')")
    @Operation(summary = "Reservas por propiedad", description = "Lista las reservas de una propiedad específica")
    public ResponseEntity<ApiResponse<Page<ReservaResumenDTO>>> listarReservasPorPropiedad(
            @PathVariable Long propiedadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservaResumenDTO> reservas = reservaService.listarReservasPorPropiedad(propiedadId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas exitosamente", reservas));
    }
}
