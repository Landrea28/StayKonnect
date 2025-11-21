package com.staykonnect.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para mensajes enviados por WebSocket (tiempo real)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeWebSocketDTO {

    private Long id;
    private String contenido;
    private LocalDateTime fecha;

    // Remitente
    private Long remitenteId;
    private String remitenteNombre;
    private String remitenteFotoPerfil;

    // Destinatario
    private Long destinatarioId;

    // Tipo de evento
    private TipoEvento tipo;

    // Reserva asociada (opcional)
    private Long reservaId;

    public enum TipoEvento {
        MENSAJE_NUEVO,
        MENSAJE_LEIDO,
        USUARIO_ESCRIBIENDO,
        USUARIO_CONECTADO,
        USUARIO_DESCONECTADO
    }
}
