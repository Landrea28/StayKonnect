package com.staykonnect.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leido = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    // Auditoría
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    // Métodos de utilidad
    public void marcarComoLeido() {
        this.leido = true;
        this.fechaLectura = LocalDateTime.now();
    }

    public boolean esRemitenteODestinatario(Long usuarioId) {
        return this.remitente.getId().equals(usuarioId) || 
               this.destinatario.getId().equals(usuarioId);
    }
}
