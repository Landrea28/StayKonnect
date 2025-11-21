package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.TipoNotificacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Notificación - Representa notificaciones enviadas a los usuarios
 */
@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_usuario", columnList = "usuario_id"),
    @Index(name = "idx_tipo", columnList = "tipo"),
    @Index(name = "idx_leida", columnList = "leida"),
    @Index(name = "idx_fecha", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de notificación es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoNotificacion tipo;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Column(name = "enviada_email")
    @Builder.Default
    private Boolean enviadaEmail = false;

    @Column(name = "enviada_push")
    @Builder.Default
    private Boolean enviadaPush = false;

    @Column(length = 500)
    private String enlace;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Métodos auxiliares
    public void marcarComoLeida() {
        this.leida = true;
        this.fechaLectura = LocalDateTime.now();
    }

    public boolean esAlerta() {
        return tipo == TipoNotificacion.SISTEMA_ALERTA || 
               tipo == TipoNotificacion.USUARIO_BANEADO ||
               tipo == TipoNotificacion.RESERVA_CANCELADA_ADMIN;
    }
    
    public boolean requiereEmail() {
        return tipo == TipoNotificacion.REGISTRO_EXITOSO ||
               tipo == TipoNotificacion.EMAIL_VERIFICADO ||
               tipo == TipoNotificacion.PASSWORD_RECUPERADA ||
               tipo == TipoNotificacion.RESERVA_CONFIRMADA ||
               tipo == TipoNotificacion.PAGO_EXITOSO ||
               tipo == TipoNotificacion.USUARIO_BANEADO ||
               tipo == TipoNotificacion.PROPIEDAD_APROBADA ||
               tipo == TipoNotificacion.PROPIEDAD_RECHAZADA;
    }
}
