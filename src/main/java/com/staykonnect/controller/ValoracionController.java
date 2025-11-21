package com.staykonnect.controller;

import com.staykonnect.dto.ApiResponse;
import com.staykonnect.dto.valoracion.*;
import com.staykonnect.infrastructure.security.annotation.CurrentUser;
import com.staykonnect.security.UserPrincipal;
import com.staykonnect.service.ValoracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
@Tag(name = "Valoraciones", description = "Gestión de valoraciones y reputación")
@SecurityRequirement(name = "bearerAuth")
public class ValoracionController {

    private final ValoracionService valoracionService;

    @PostMapping("/reserva/{reservaId}")
    @PreAuthorize("hasRole('VIAJERO')")
    @Operation(summary = "Crear valoración", description = "Crear valoración de una reserva completada")
    public ResponseEntity<ApiResponse<ValoracionDTO>> crearValoracion(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long reservaId,
            @Valid @RequestBody CrearValoracionRequest request
    ) {
        ValoracionDTO valoracion = valoracionService.crearValoracion(
                reservaId, currentUser.getId(), request
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Valoración creada exitosamente. ¡Gracias por tu opinión!", valoracion
        ));
    }

    @PutMapping("/{valoracionId}/responder")
    @PreAuthorize("hasRole('ANFITRION')")
    @Operation(summary = "Responder valoración", description = "El anfitrión responde a una valoración")
    public ResponseEntity<ApiResponse<ValoracionDTO>> responderValoracion(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long valoracionId,
            @Valid @RequestBody ResponderValoracionRequest request
    ) {
        ValoracionDTO valoracion = valoracionService.responderValoracion(
                valoracionId, currentUser.getId(), request
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Respuesta publicada exitosamente", valoracion
        ));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @Operation(summary = "Valoraciones de propiedad", description = "Obtener todas las valoraciones de una propiedad")
    public ResponseEntity<ApiResponse<Page<ValoracionDTO>>> obtenerValoracionesPropiedad(
            @PathVariable Long propiedadId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ValoracionDTO> valoraciones = valoracionService.obtenerValoracionesPropiedad(
                propiedadId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Valoraciones obtenidas exitosamente", valoraciones
        ));
    }

    @GetMapping("/anfitrion/{anfitrionId}")
    @Operation(summary = "Valoraciones de anfitrión", description = "Obtener todas las valoraciones recibidas por un anfitrión")
    public ResponseEntity<ApiResponse<Page<ValoracionDTO>>> obtenerValoracionesAnfitrion(
            @PathVariable Long anfitrionId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ValoracionDTO> valoraciones = valoracionService.obtenerValoracionesAnfitrion(
                anfitrionId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Valoraciones obtenidas exitosamente", valoraciones
        ));
    }

    @GetMapping("/mis-valoraciones")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Mis valoraciones", description = "Obtener valoraciones realizadas por el usuario actual")
    public ResponseEntity<ApiResponse<Page<ValoracionDTO>>> obtenerMisValoraciones(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ValoracionDTO> valoraciones = valoracionService.obtenerMisValoraciones(
                currentUser.getId(), pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Tus valoraciones obtenidas exitosamente", valoraciones
        ));
    }

    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Valoración de reserva", description = "Obtener valoración de una reserva específica")
    public ResponseEntity<ApiResponse<ValoracionDTO>> obtenerValoracionPorReserva(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long reservaId
    ) {
        ValoracionDTO valoracion = valoracionService.obtenerValoracionPorReserva(
                reservaId, currentUser.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Valoración obtenida exitosamente", valoracion
        ));
    }

    @GetMapping("/propiedad/{propiedadId}/estadisticas")
    @Operation(summary = "Estadísticas de propiedad", description = "Obtener estadísticas de valoraciones de una propiedad")
    public ResponseEntity<ApiResponse<EstadisticasValoracionDTO>> obtenerEstadisticasPropiedad(
            @PathVariable Long propiedadId
    ) {
        EstadisticasValoracionDTO estadisticas = valoracionService.obtenerEstadisticasPropiedad(propiedadId);
        return ResponseEntity.ok(ApiResponse.success(
                "Estadísticas obtenidas exitosamente", estadisticas
        ));
    }

    @GetMapping("/pendientes-respuesta")
    @PreAuthorize("hasRole('ANFITRION')")
    @Operation(summary = "Valoraciones pendientes", description = "Obtener valoraciones pendientes de respuesta del anfitrión")
    public ResponseEntity<ApiResponse<Page<ValoracionDTO>>> obtenerValoracionesPendientes(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ValoracionDTO> valoraciones = valoracionService.obtenerValoracionesPendientesRespuesta(
                currentUser.getId(), pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Valoraciones pendientes obtenidas exitosamente", valoraciones
        ));
    }

    @GetMapping("/reserva/{reservaId}/puede-valorar")
    @PreAuthorize("hasRole('VIAJERO')")
    @Operation(summary = "Verificar si puede valorar", description = "Verificar si el usuario puede valorar una reserva")
    public ResponseEntity<ApiResponse<Boolean>> puedeValorarReserva(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long reservaId
    ) {
        boolean puede = valoracionService.puedeValorarReserva(reservaId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(
                puede ? "Puedes valorar esta reserva" : "No puedes valorar esta reserva",
                puede
        ));
    }
}
