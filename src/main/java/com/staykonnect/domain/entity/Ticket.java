package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoTicket;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Ticket - Representa solicitudes de soporte y disputas
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_usuario", columnList = "usuario_id"),
    @Index(name = "idx_reserva", columnList = "reserva_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_prioridad", columnList = "prioridad")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_ticket", nullable = false, unique = true, length = 20)
    private String codigoTicket;

    @NotBlank(message = "El asunto es obligatorio")
    @Column(nullable = false, length = 200)
    private String asunto;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoTicket estado = EstadoTicket.ABIERTO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String prioridad = "MEDIA"; // BAJA, MEDIA, ALTA, URGENTE

    @Column(name = "categoria", length = 50)
    private String categoria; // CANCELACION, REEMBOLSO, QUEJA, DISPUTA, TECNICO, OTRO

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "tiempo_respuesta_horas")
    private Integer tiempoRespuestaHoras;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    @Column(name = "agente_asignado")
    private String agenteAsignado;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    // Métodos auxiliares
    public boolean estaAbierto() {
        return estado == EstadoTicket.ABIERTO || 
               estado == EstadoTicket.ASIGNADO || 
               estado == EstadoTicket.EN_PROCESO;
    }

    public boolean estaResuelto() {
        return estado == EstadoTicket.RESUELTO || estado == EstadoTicket.CERRADO;
    }

    public void cerrar() {
        this.estado = EstadoTicket.CERRADO;
        this.fechaCierre = LocalDateTime.now();
    }

    public boolean esUrgente() {
        return "URGENTE".equalsIgnoreCase(prioridad);
    }
}
