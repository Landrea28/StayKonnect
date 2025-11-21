package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoVerificacion;
import com.staykonnect.domain.enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidad Documento - Representa documentos legales de propiedades
 */
@Entity
@Table(name = "documentos", indexes = {
    @Index(name = "idx_propiedad", columnList = "propiedad_id"),
    @Index(name = "idx_tipo", columnList = "tipo"),
    @Index(name = "idx_estado", columnList = "estado_verificacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Documento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDocumento tipo;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(name = "url_archivo", nullable = false)
    private String urlArchivo;

    @Column(name = "numero_documento", length = 100)
    private String numeroDocumento;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false, length = 20)
    @Builder.Default
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "verificado_por")
    private String verificadoPor;

    @Column(name = "fecha_verificacion")
    private java.time.LocalDateTime fechaVerificacion;

    // Relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    // Métodos auxiliares
    public boolean estaAprobado() {
        return estadoVerificacion == EstadoVerificacion.APROBADO;
    }

    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    public boolean estaPendiente() {
        return estadoVerificacion == EstadoVerificacion.PENDIENTE || 
               estadoVerificacion == EstadoVerificacion.EN_REVISION;
    }
}
