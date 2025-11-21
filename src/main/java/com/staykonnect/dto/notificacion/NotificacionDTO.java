package com.staykonnect.dto.notificacion;

import com.staykonnect.domain.enums.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private Long id;
    private TipoNotificacion tipo;
    private String titulo;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaLectura;
    private String enlace;
    private Boolean enviadaEmail;
    private LocalDateTime createdDate;
}
