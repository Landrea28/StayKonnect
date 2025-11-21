package com.staykonnect.controller;

import com.staykonnect.dto.ApiResponse;
import com.staykonnect.dto.admin.*;
import com.staykonnect.domain.entity.enums.EstadoReserva;
import com.staykonnect.domain.entity.enums.Rol;
import com.staykonnect.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para funcionalidades de administración
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Endpoints para gestión administrativa del sistema")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener métricas del dashboard", description = "Retorna todas las métricas del sistema para el panel de administración")
    public ResponseEntity<ApiResponse<DashboardMetricasDTO>> obtenerDashboard() {
        DashboardMetricasDTO metricas = adminService.obtenerMetricasDashboard();
        return ResponseEntity.ok(ApiResponse.success(metricas, "Métricas obtenidas exitosamente"));
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    @GetMapping("/usuarios")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios del sistema con paginación")
    public ResponseEntity<ApiResponse<Page<UsuarioAdminDTO>>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direccion), ordenarPor);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UsuarioAdminDTO> usuarios = adminService.listarUsuarios(pageable);
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Usuarios obtenidos exitosamente"));
    }

    @GetMapping("/usuarios/buscar")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre, email o teléfono")
    public ResponseEntity<ApiResponse<Page<UsuarioAdminDTO>>> buscarUsuarios(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Rol rol,
            @RequestParam(required = false) Boolean baneado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<UsuarioAdminDTO> usuarios = adminService.buscarUsuarios(query, rol, baneado, pageable);
        return ResponseEntity.ok(ApiResponse.success(usuarios, "Búsqueda completada"));
    }

    @PutMapping("/usuarios/{id}/banear")
    @Operation(summary = "Banear usuario", description = "Banea a un usuario del sistema")
    public ResponseEntity<ApiResponse<UsuarioAdminDTO>> banearUsuario(
            @PathVariable Long id,
            @Valid @RequestBody BanearUsuarioRequest request
    ) {
        UsuarioAdminDTO usuario = adminService.banearUsuario(id, request);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario baneado exitosamente"));
    }

    @PutMapping("/usuarios/{id}/desbanear")
    @Operation(summary = "Desbanear usuario", description = "Remueve el baneo de un usuario")
    public ResponseEntity<ApiResponse<UsuarioAdminDTO>> desbanearUsuario(@PathVariable Long id) {
        UsuarioAdminDTO usuario = adminService.desbanearUsuario(id);
        return ResponseEntity.ok(ApiResponse.success(usuario, "Usuario desbaneado exitosamente"));
    }

    @DeleteMapping("/usuarios/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema (solo si no tiene reservas activas)")
    public ResponseEntity<ApiResponse<Void>> eliminarUsuario(@PathVariable Long id) {
        adminService.eliminarUsuario(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"));
    }

    // ==================== GESTIÓN DE PROPIEDADES ====================

    @GetMapping("/propiedades")
    @Operation(summary = "Listar propiedades", description = "Lista todas las propiedades con paginación")
    public ResponseEntity<ApiResponse<Page<PropiedadAdminDTO>>> listarPropiedades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direccion), ordenarPor);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PropiedadAdminDTO> propiedades = adminService.listarPropiedades(pageable);
        return ResponseEntity.ok(ApiResponse.success(propiedades, "Propiedades obtenidas exitosamente"));
    }

    @GetMapping("/propiedades/pendientes")
    @Operation(summary = "Listar propiedades pendientes", description = "Lista propiedades pendientes de aprobación")
    public ResponseEntity<ApiResponse<Page<PropiedadAdminDTO>>> listarPropiedadesPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").ascending());
        Page<PropiedadAdminDTO> propiedades = adminService.listarPropiedadesPendientes(pageable);
        return ResponseEntity.ok(ApiResponse.success(propiedades, "Propiedades pendientes obtenidas"));
    }

    @PutMapping("/propiedades/{id}/aprobar")
    @Operation(summary = "Aprobar propiedad", description = "Aprueba una propiedad para publicación")
    public ResponseEntity<ApiResponse<PropiedadAdminDTO>> aprobarPropiedad(@PathVariable Long id) {
        PropiedadAdminDTO propiedad = adminService.aprobarPropiedad(id);
        return ResponseEntity.ok(ApiResponse.success(propiedad, "Propiedad aprobada exitosamente"));
    }

    @PutMapping("/propiedades/{id}/rechazar")
    @Operation(summary = "Rechazar propiedad", description = "Rechaza una propiedad con una razón")
    public ResponseEntity<ApiResponse<PropiedadAdminDTO>> rechazarPropiedad(
            @PathVariable Long id,
            @Valid @RequestBody RechazarPropiedadRequest request
    ) {
        PropiedadAdminDTO propiedad = adminService.rechazarPropiedad(id, request);
        return ResponseEntity.ok(ApiResponse.success(propiedad, "Propiedad rechazada"));
    }

    @PutMapping("/propiedades/{id}/toggle-visibilidad")
    @Operation(summary = "Cambiar visibilidad", description = "Oculta o muestra una propiedad")
    public ResponseEntity<ApiResponse<PropiedadAdminDTO>> toggleVisibilidadPropiedad(@PathVariable Long id) {
        PropiedadAdminDTO propiedad = adminService.toggleVisibilidadPropiedad(id);
        return ResponseEntity.ok(ApiResponse.success(propiedad, "Visibilidad cambiada"));
    }

    // ==================== GESTIÓN DE RESERVAS ====================

    @GetMapping("/reservas")
    @Operation(summary = "Listar reservas", description = "Lista todas las reservas con paginación")
    public ResponseEntity<ApiResponse<Page<ReservaAdminDTO>>> listarReservas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direccion
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direccion), ordenarPor);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ReservaAdminDTO> reservas = adminService.listarReservas(pageable);
        return ResponseEntity.ok(ApiResponse.success(reservas, "Reservas obtenidas exitosamente"));
    }

    @GetMapping("/reservas/estado/{estado}")
    @Operation(summary = "Buscar reservas por estado", description = "Lista reservas filtradas por estado")
    public ResponseEntity<ApiResponse<Page<ReservaAdminDTO>>> buscarReservasPorEstado(
            @PathVariable EstadoReserva estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<ReservaAdminDTO> reservas = adminService.buscarReservasPorEstado(estado, pageable);
        return ResponseEntity.ok(ApiResponse.success(reservas, "Reservas filtradas obtenidas"));
    }

    @PutMapping("/reservas/{id}/cancelar")
    @Operation(summary = "Cancelar reserva (Admin)", description = "Permite al admin cancelar cualquier reserva")
    public ResponseEntity<ApiResponse<ReservaAdminDTO>> cancelarReserva(
            @PathVariable Long id,
            @RequestParam String razon
    ) {
        ReservaAdminDTO reserva = adminService.cancelarReserva(id, razon);
        return ResponseEntity.ok(ApiResponse.success(reserva, "Reserva cancelada por administrador"));
    }

    // ==================== MODERACIÓN DE VALORACIONES ====================

    @PutMapping("/valoraciones/{id}/ocultar")
    @Operation(summary = "Ocultar valoración", description = "Oculta una valoración inapropiada")
    public ResponseEntity<ApiResponse<Void>> ocultarValoracion(@PathVariable Long id) {
        adminService.ocultarValoracion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Valoración ocultada"));
    }

    @PutMapping("/valoraciones/{id}/mostrar")
    @Operation(summary = "Mostrar valoración", description = "Muestra una valoración previamente oculta")
    public ResponseEntity<ApiResponse<Void>> mostrarValoracion(@PathVariable Long id) {
        adminService.mostrarValoracion(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Valoración mostrada"));
    }
}
