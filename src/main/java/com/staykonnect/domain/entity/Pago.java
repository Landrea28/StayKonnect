package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoPago;
import com.staykonnect.domain.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Pago - Representa un pago realizado por una reserva
 */
@Entity
@Table(name = "pagos", indexes = {
    @Index(name = "idx_reserva", columnList = "reserva_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_transaccion", columnList = "id_transaccion_externa")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El monto es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "id_transaccion_externa", unique = true)
    private String idTransaccionExterna;

    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;

    @Column(name = "fecha_liberacion")
    private LocalDateTime fechaLiberacion;

    @Column(name = "fecha_retencion")
    private LocalDateTime fechaRetencion;

    @Column(name = "comision_plataforma", precision = 10, scale = 2)
    private BigDecimal comisionPlataforma;

    @Column(name = "monto_anfitrion", precision = 10, scale = 2)
    private BigDecimal montoAnfitrion;

    @Column(columnDefinition = "TEXT")
    private String detalles;

    @Column(name = "motivo_reembolso", columnDefinition = "TEXT")
    private String motivoReembolso;

    @Column(name = "fecha_reembolso")
    private LocalDateTime fechaReembolso;

    // Relación
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;

    // Métodos auxiliares
    public boolean estaCompletado() {
        return estado == EstadoPago.COMPLETADO || estado == EstadoPago.RETENIDO;
    }

    public boolean estaLiberado() {
        return estado == EstadoPago.LIBERADO;
    }

    public boolean puedeSerReembolsado() {
        return estado == EstadoPago.COMPLETADO || 
               estado == EstadoPago.RETENIDO || 
               estado == EstadoPago.DISPUTADO;
    }

    public void calcularMontoAnfitrion() {
        if (monto != null && comisionPlataforma != null) {
            this.montoAnfitrion = monto.subtract(comisionPlataforma);
        }
    }
}
