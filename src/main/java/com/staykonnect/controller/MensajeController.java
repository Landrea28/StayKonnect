package com.staykonnect.controller;

import com.staykonnect.dto.ApiResponse;
import com.staykonnect.dto.mensaje.ConversacionDTO;
import com.staykonnect.dto.mensaje.EnviarMensajeRequest;
import com.staykonnect.dto.mensaje.MensajeDTO;
import com.staykonnect.infrastructure.security.annotation.CurrentUser;
import com.staykonnect.security.UserPrincipal;
import com.staykonnect.service.MensajeService;
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

import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@RequiredArgsConstructor
@Tag(name = "Mensajes", description = "Gestión de mensajería entre usuarios")
@SecurityRequirement(name = "bearerAuth")
public class MensajeController {

    private final MensajeService mensajeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Enviar mensaje", description = "Enviar un mensaje a otro usuario")
    public ResponseEntity<ApiResponse<MensajeDTO>> enviarMensaje(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody EnviarMensajeRequest request
    ) {
        MensajeDTO mensaje = mensajeService.enviarMensaje(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Mensaje enviado exitosamente", mensaje));
    }

    @GetMapping("/conversaciones")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Listar conversaciones", description = "Obtener todas las conversaciones del usuario actual")
    public ResponseEntity<ApiResponse<Page<ConversacionDTO>>> obtenerConversaciones(
            @CurrentUser UserPrincipal currentUser,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ConversacionDTO> conversaciones = mensajeService.obtenerConversaciones(
                currentUser.getId(), pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Conversaciones obtenidas exitosamente", conversaciones
        ));
    }

    @GetMapping("/conversacion/{otroUsuarioId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Obtener conversación", description = "Obtener mensajes de una conversación con otro usuario")
    public ResponseEntity<ApiResponse<Page<MensajeDTO>>> obtenerConversacion(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long otroUsuarioId,
            @PageableDefault(size = 50, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<MensajeDTO> mensajes = mensajeService.obtenerConversacion(
                currentUser.getId(), otroUsuarioId, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Conversación obtenida exitosamente", mensajes
        ));
    }

    @PutMapping("/{mensajeId}/leer")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Marcar mensaje como leído", description = "Marcar un mensaje específico como leído")
    public ResponseEntity<ApiResponse<Void>> marcarComoLeido(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long mensajeId
    ) {
        mensajeService.marcarComoLeido(mensajeId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Mensaje marcado como leído", null));
    }

    @PutMapping("/conversacion/{otroUsuarioId}/leer")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Marcar conversación como leída", 
               description = "Marcar todos los mensajes de una conversación como leídos")
    public ResponseEntity<ApiResponse<Void>> marcarConversacionComoLeida(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long otroUsuarioId
    ) {
        mensajeService.marcarConversacionComoLeida(currentUser.getId(), otroUsuarioId);
        return ResponseEntity.ok(ApiResponse.success(
                "Conversación marcada como leída", null
        ));
    }

    @GetMapping("/no-leidos/count")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Contar mensajes no leídos", 
               description = "Obtener cantidad de mensajes no leídos del usuario")
    public ResponseEntity<ApiResponse<Long>> contarMensajesNoLeidos(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long count = mensajeService.contarMensajesNoLeidos(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(
                "Cantidad de mensajes no leídos", count
        ));
    }

    @GetMapping("/reserva/{reservaId}")
    @PreAuthorize("hasAnyRole('VIAJERO', 'ANFITRION')")
    @Operation(summary = "Mensajes por reserva", 
               description = "Obtener todos los mensajes relacionados con una reserva")
    public ResponseEntity<ApiResponse<List<MensajeDTO>>> obtenerMensajesPorReserva(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long reservaId
    ) {
        List<MensajeDTO> mensajes = mensajeService.obtenerMensajesPorReserva(
                reservaId, currentUser.getId()
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Mensajes de la reserva obtenidos exitosamente", mensajes
        ));
    }
}
