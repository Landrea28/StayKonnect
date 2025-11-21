package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoReserva;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Reserva - Representa una reserva de una propiedad por un viajero
 */
@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_viajero", columnList = "viajero_id"),
    @Index(name = "idx_propiedad", columnList = "propiedad_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_fechas", columnList = "fecha_checkin,fecha_checkout")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_reserva", nullable = false, unique = true, length = 20)
    private String codigoReserva;

    @NotNull(message = "La fecha de check-in es obligatoria")
    @Column(name = "fecha_checkin", nullable = false)
    private LocalDate fechaCheckin;

    @NotNull(message = "La fecha de check-out es obligatoria")
    @Column(name = "fecha_checkout", nullable = false)
    private LocalDate fechaCheckout;

    @NotNull(message = "El número de huéspedes es obligatorio")
    @Column(name = "numero_huespedes", nullable = false)
    private Integer numeroHuespedes;

    // Fechas reales de check-in/out
    @Column(name = "checkin_realizado")
    private LocalDateTime checkinRealizado;

    @Column(name = "checkout_realizado")
    private LocalDateTime checkoutRealizado;

    // Costos
    @Column(name = "precio_por_noche", precision = 10, scale = 2)
    private BigDecimal precioPorNoche;

    @Column(name = "numero_noches")
    private Integer numeroNoches;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "precio_limpieza", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioLimpieza = BigDecimal.ZERO;

    @Column(name = "comision_plataforma", precision = 10, scale = 2)
    private BigDecimal comisionPlataforma;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viajero_id", nullable = false)
    private Usuario viajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propiedad_id", nullable = false)
    private Propiedad propiedad;

    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Mensaje> mensajes = new HashSet<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Valoracion> valoraciones = new HashSet<>();

    // Métodos auxiliares
    @PrePersist
    public void calcularCostos() {
        if (fechaCheckin != null && fechaCheckout != null && precioPorNoche != null) {
            this.numeroNoches = (int) ChronoUnit.DAYS.between(fechaCheckin, fechaCheckout);
            this.subtotal = precioPorNoche.multiply(BigDecimal.valueOf(numeroNoches));
            
            // Comisión del 10% para la plataforma
            this.comisionPlataforma = subtotal.multiply(BigDecimal.valueOf(0.10));
            
            this.total = subtotal.add(precioLimpieza).add(comisionPlataforma);
        }
    }

    public boolean puedeSerCancelada() {
        return estado == EstadoReserva.PENDIENTE || 
               estado == EstadoReserva.CONFIRMADA || 
               estado == EstadoReserva.PAGADA;
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.CONFIRMADA || 
               estado == EstadoReserva.PAGADA || 
               estado == EstadoReserva.EN_CURSO;
    }

    public boolean estaCompletada() {
        return estado == EstadoReserva.COMPLETADA;
    }

    public boolean puedeSerValorada() {
        return estado == EstadoReserva.COMPLETADA && checkoutRealizado != null;
    }
}
