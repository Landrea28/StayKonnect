package com.staykonnect.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "valoracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Valoracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer puntuacion; // 1-5 estrellas

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "respuesta_anfitrion", columnDefinition = "TEXT")
    private String respuestaAnfitrion;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(nullable = false)
    @Builder.Default
    private Boolean visible = true;

    // Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valorador_id", nullable = false)
    private Usuario valorador; // Quien valora (viajero)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valorado_id", nullable = false)
    private Usuario valorado; // Quien es valorado (anfitrión)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    // Auditoría
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Métodos de utilidad
    public void responder(String respuesta) {
        this.respuestaAnfitrion = respuesta;
        this.fechaRespuesta = LocalDateTime.now();
    }

    public boolean tieneRespuesta() {
        return this.respuestaAnfitrion != null && !this.respuestaAnfitrion.isBlank();
    }

    public boolean puedeSerRespondidaPor(Long usuarioId) {
        return this.valorado.getId().equals(usuarioId);
    }
}
