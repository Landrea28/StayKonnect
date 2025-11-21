package com.staykonnect.dto.valoracion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValoracionDTO {

    private Long id;
    private Integer puntuacion;
    private String comentario;
    private String respuestaAnfitrion;
    private LocalDateTime fechaRespuesta;
    private Boolean visible;
    private LocalDateTime createdDate;

    // Reserva
    private Long reservaId;
    private LocalDateTime fechaCheckin;
    private LocalDateTime fechaCheckout;

    // Valorador (quien valora)
    private Long valoradorId;
    private String valoradorNombre;
    private String valoradorFotoPerfil;

    // Valorado (anfitrión)
    private Long valoradoId;
    private String valoradoNombre;
    private String valoradoFotoPerfil;

    // Propiedad
    private Long propiedadId;
    private String propiedadTitulo;
    private String propiedadImagenPrincipal;
}
