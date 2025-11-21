package com.staykonnect.controller;

import com.staykonnect.domain.enums.TipoNotificacion;
import com.staykonnect.dto.notificacion.NotificacionDTO;
import com.staykonnect.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de notificaciones
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para gestión de notificaciones de usuarios")
@PreAuthorize("isAuthenticated()")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    @Operation(summary = "Listar mis notificaciones (paginado)")
    public ResponseEntity<Page<NotificacionDTO>> listarMisNotificaciones(
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<NotificacionDTO> notificaciones = notificacionService.listarMisNotificaciones(pageable);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/no-leidas/count")
    @Operation(summary = "Contar notificaciones no leídas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        long count = notificacionService.contarNoLeidas();
        return ResponseEntity.ok(Map.of("noLeidas", count));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Obtener notificaciones por tipo")
    public ResponseEntity<List<NotificacionDTO>> obtenerPorTipo(@PathVariable TipoNotificacion tipo) {
        List<NotificacionDTO> notificaciones = notificacionService.obtenerPorTipo(tipo);
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de notificaciones")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> estadisticas = notificacionService.obtenerEstadisticas();
        return ResponseEntity.ok(estadisticas);
    }

    @PutMapping("/{id}/leer")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long id) {
        NotificacionDTO notificacion = notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok(notificacion);
    }

    @PutMapping("/marcar-todas-leidas")
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    public ResponseEntity<Map<String, String>> marcarTodasComoLeidas() {
        notificacionService.marcarTodasComoLeidas();
        return ResponseEntity.ok(Map.of("mensaje", "Todas las notificaciones han sido marcadas como leídas"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}
