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
public class MensajeDTO {

    private Long id;
    private String contenido;
    private Boolean leido;
    private LocalDateTime fechaLectura;
    private LocalDateTime createdDate;

    // Remitente
    private Long remitenteId;
    private String remitenteNombre;
    private String remitenteEmail;
    private String remitenteFotoPerfil;

    // Destinatario
    private Long destinatarioId;
    private String destinatarioNombre;
    private String destinatarioEmail;
    private String destinatarioFotoPerfil;

    // Reserva asociada (opcional)
    private Long reservaId;
    private String reservaTitulo; // Título de la propiedad
}
