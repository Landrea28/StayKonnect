package com.staykonnect.dto.mensaje;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversacionDTO {

    // Usuario con quien se tiene la conversación
    private Long otroUsuarioId;
    private String otroUsuarioNombre;
    private String otroUsuarioEmail;
    private String otroUsuarioFotoPerfil;

    // Último mensaje
    private Long ultimoMensajeId;
    private String ultimoMensajeContenido;
    private LocalDateTime ultimoMensajeFecha;
    private Boolean ultimoMensajeLeido;
    
    // Indica si el usuario actual envió el último mensaje
    private Boolean yoEnvieUltimoMensaje;

    // Cantidad de mensajes no leídos en esta conversación
    private Long mensajesNoLeidos;
}
