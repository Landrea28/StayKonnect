package com.staykonnect.controller;

import com.staykonnect.common.dto.ApiResponse;
import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.TipoPropiedad;
import com.staykonnect.dto.propiedad.*;
import com.staykonnect.service.BusquedaPropiedadService;
import com.staykonnect.service.PropiedadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para la gestión de propiedades.
 * Implementa el CRUD completo con control de acceso basado en roles.
 */
@RestController
@RequestMapping("/api/propiedades")
@RequiredArgsConstructor
@Tag(name = "Propiedades", description = "Endpoints para gestión de propiedades de alquiler")
public class PropiedadController {

    private final PropiedadService propiedadService;
    private final BusquedaPropiedadService busquedaPropiedadService;

    /**
     * Crea una nueva propiedad.
     * Solo anfitriones pueden crear propiedades.
     *
     * @param request Datos de la propiedad
     * @return Propiedad creada
     */
    @PostMapping
    @PreAuthorize("hasRole('ANFITRION')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear propiedad", description = "Crea una nueva propiedad. Solo para anfitriones.")
    public ResponseEntity<ApiResponse<PropiedadDTO>> crearPropiedad(
            @Valid @RequestBody CrearPropiedadRequest request) {
        PropiedadDTO propiedad = propiedadService.crearPropiedad(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Propiedad creada exitosamente", propiedad));
    }

    /**
     * Obtiene los detalles de una propiedad.
     * Endpoint público.
     *
     * @param id ID de la propiedad
     * @return Propiedad encontrada
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener propiedad", description = "Obtiene los detalles completos de una propiedad por ID")
    public ResponseEntity<ApiResponse<PropiedadDTO>> obtenerPropiedad(@PathVariable Long id) {
        PropiedadDTO propiedad = propiedadService.obtenerPropiedad(id);
        return ResponseEntity.ok(ApiResponse.success("Propiedad encontrada", propiedad));
    }

    /**
     * Lista las propiedades del anfitrión autenticado.
     * Solo anfitriones pueden acceder.
     *
     * @param page Número de página (default: 0)
     * @param size Tamaño de página (default: 10)
     * @param sort Campo de ordenamiento (default: createdDate)
     * @param direction Dirección de ordenamiento (default: DESC)
     * @return Página de propiedades
     */
    @GetMapping("/mis-propiedades")
    @PreAuthorize("hasRole('ANFITRION')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Mis propiedades", description = "Lista todas las propiedades del anfitrión autenticado")
    public ResponseEntity<ApiResponse<Page<PropiedadResumenDTO>>> listarMisPropiedades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<PropiedadResumenDTO> propiedades = propiedadService.listarMisPropiedades(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Propiedades obtenidas exitosamente", 
                propiedades
        ));
    }

    /**
     * Busca propiedades con filtros avanzados (público).
     *
     * @param query Texto a buscar en título/descripción
     * @param ciudad Ciudad de la propiedad
     * @param pais País de la propiedad
     * @param fechaInicio Fecha de inicio de disponibilidad
     * @param fechaFin Fecha de fin de disponibilidad
     * @param tipoPropiedad Tipo de propiedad
     * @param capacidadMinima Capacidad mínima de huéspedes
     * @param habitacionesMinimas Número mínimo de habitaciones
     * @param camasMinimas Número mínimo de camas
     * @param banosMinimos Número mínimo de baños
     * @param precioMinimo Precio mínimo por noche
     * @param precioMaximo Precio máximo por noche
     * @param servicios Lista de servicios requeridos
     * @param puntuacionMinima Puntuación mínima
     * @param ordenarPor Campo de ordenamiento (precio, puntuacion, reciente, relevancia)
     * @param direccion Dirección de ordenamiento (asc, desc)
     * @param page Número de página
     * @param size Tamaño de página
     * @return Página de propiedades que cumplen los criterios
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar propiedades", description = "Búsqueda avanzada de propiedades con múltiples filtros")
    public ResponseEntity<ApiResponse<Page<PropiedadResumenDTO>>> buscarPropiedades(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) TipoPropiedad tipoPropiedad,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Integer habitacionesMinimas,
            @RequestParam(required = false) Integer camasMinimas,
            @RequestParam(required = false) Integer banosMinimos,
            @RequestParam(required = false) BigDecimal precioMinimo,
            @RequestParam(required = false) BigDecimal precioMaximo,
            @RequestParam(required = false) List<String> servicios,
            @RequestParam(required = false) Double puntuacionMinima,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String direccion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        BusquedaPropiedadRequest request = BusquedaPropiedadRequest.builder()
                .query(query)
                .ciudad(ciudad)
                .pais(pais)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .tipoPropiedad(tipoPropiedad)
                .capacidadMinima(capacidadMinima)
                .habitacionesMinimas(habitacionesMinimas)
                .camasMinimas(camasMinimas)
                .banosMinimos(banosMinimos)
                .precioMinimo(precioMinimo)
                .precioMaximo(precioMaximo)
                .servicios(servicios)
                .puntuacionMinima(puntuacionMinima)
                .ordenarPor(ordenarPor)
                .direccion(direccion)
                .build();

        Pageable pageable = PageRequest.of(page, size);
        Page<PropiedadResumenDTO> resultados = busquedaPropiedadService.buscarPropiedades(request, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Se encontraron %d propiedades", resultados.getTotalElements()),
                resultados
        ));
    }

    /**
     * Lista propiedades activas (público).
     *
     * @param page Número de página (default: 0)
     * @param size Tamaño de página (default: 20)
     * @param sort Campo de ordenamiento (default: puntuacionPromedio)
     * @param direction Dirección de ordenamiento (default: DESC)
     * @return Página de propiedades activas
     */
    @GetMapping
    @Operation(summary = "Listar propiedades activas", description = "Lista todas las propiedades disponibles para alquiler")
    public ResponseEntity<ApiResponse<Page<PropiedadResumenDTO>>> listarPropiedadesActivas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "puntuacionPromedio") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<PropiedadResumenDTO> propiedades = propiedadService.listarPropiedadesActivas(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Propiedades obtenidas exitosamente", 
                propiedades
        ));
    }

    /**
     * Actualiza una propiedad existente.
     * Solo el anfitrión propietario o admin puede actualizar.
     *
     * @param id ID de la propiedad
     * @param request Datos a actualizar
     * @return Propiedad actualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar propiedad", description = "Actualiza los datos de una propiedad existente")
    public ResponseEntity<ApiResponse<PropiedadDTO>> actualizarPropiedad(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPropiedadRequest request) {
        
        PropiedadDTO propiedad = propiedadService.actualizarPropiedad(id, request);
        return ResponseEntity.ok(ApiResponse.success("Propiedad actualizada exitosamente", propiedad));
    }

    /**
     * Cambia el estado de una propiedad.
     * Solo el anfitrión propietario o admin puede cambiar el estado.
     *
     * @param id ID de la propiedad
     * @param estado Nuevo estado
     * @return Propiedad actualizada
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cambiar estado de propiedad", description = "Cambia el estado de una propiedad (ACTIVA, INACTIVA, etc.)")
    public ResponseEntity<ApiResponse<PropiedadDTO>> cambiarEstadoPropiedad(
            @PathVariable Long id,
            @RequestParam EstadoPropiedad estado) {
        
        PropiedadDTO propiedad = propiedadService.cambiarEstadoPropiedad(id, estado);
        return ResponseEntity.ok(ApiResponse.success("Estado de propiedad actualizado", propiedad));
    }

    /**
     * Elimina (lógicamente) una propiedad.
     * Solo el anfitrión propietario o admin puede eliminar.
     *
     * @param id ID de la propiedad
     * @return Mensaje de confirmación
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar propiedad", description = "Elimina una propiedad (eliminación lógica)")
    public ResponseEntity<ApiResponse<Void>> eliminarPropiedad(@PathVariable Long id) {
        propiedadService.eliminarPropiedad(id);
        return ResponseEntity.ok(ApiResponse.success("Propiedad eliminada exitosamente", null));
    }
}
