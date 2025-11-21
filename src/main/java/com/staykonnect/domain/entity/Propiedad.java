package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.TipoPropiedad;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Propiedad - Representa un alojamiento publicado por un anfitrión
 */
@Entity
@Table(name = "propiedades", indexes = {
    @Index(name = "idx_anfitrion", columnList = "anfitrion_id"),
    @Index(name = "idx_estado", columnList = "estado"),
    @Index(name = "idx_ciudad", columnList = "ciudad"),
    @Index(name = "idx_precio", columnList = "precio_por_noche")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propiedad extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @NotNull(message = "El tipo de propiedad es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_propiedad", nullable = false, length = 30)
    private TipoPropiedad tipoPropiedad;

    // Ubicación
    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
    @Column(nullable = false, length = 100)
    private String ciudad;

    @NotBlank(message = "El país es obligatorio")
    @Column(nullable = false, length = 100)
    private String pais;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(precision = 10, scale = 7)
    private Double latitud;

    @Column(precision = 10, scale = 7)
    private Double longitud;

    // Características
    @NotNull(message = "El número de habitaciones es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 habitación")
    @Column(nullable = false)
    private Integer habitaciones;

    @NotNull(message = "El número de camas es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 cama")
    @Column(nullable = false)
    private Integer camas;

    @NotNull(message = "El número de baños es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 baño")
    @Column(nullable = false)
    private Integer banos;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "area_m2")
    private Integer areaM2;

    // Precio
    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "1.0", message = "El precio debe ser mayor a 0")
    @Column(name = "precio_por_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorNoche;

    @Column(name = "precio_limpieza", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal precioLimpieza = BigDecimal.ZERO;

    @Column(name = "deposito_seguridad", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depositoSeguridad = BigDecimal.ZERO;

    // Servicios (almacenados como JSON o tabla separada)
    @ElementCollection
    @CollectionTable(name = "propiedad_servicios", joinColumns = @JoinColumn(name = "propiedad_id"))
    @Column(name = "servicio")
    @Builder.Default
    private Set<String> servicios = new HashSet<>();

    // Imágenes
    @ElementCollection
    @CollectionTable(name = "propiedad_imagenes", joinColumns = @JoinColumn(name = "propiedad_id"))
    @Column(name = "url_imagen")
    @Builder.Default
    private Set<String> imagenes = new HashSet<>();

    // Reglas de la casa
    @Column(name = "reglas_casa", columnDefinition = "TEXT")
    private String reglasCasa;

    @Column(name = "hora_checkin")
    private String horaCheckin;

    @Column(name = "hora_checkout")
    private String horaCheckout;

    @Column(name = "estancia_minima")
    @Builder.Default
    private Integer estanciaMinima = 1;

    @Column(name = "estancia_maxima")
    private Integer estanciaMaxima;

    // Estado y reputación
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoPropiedad estado = EstadoPropiedad.PENDIENTE_APROBACION;

    @Column(name = "puntuacion_promedio")
    @Builder.Default
    private Double puntuacionPromedio = 0.0;

    // Campos de administración
    @Column(name = "aprobada")
    @Builder.Default
    private Boolean aprobada = false;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobada_por_id")
    private Usuario aprobadaPor;

    @Column(name = "rechazada")
    @Builder.Default
    private Boolean rechazada = false;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @Column(name = "razon_rechazo", columnDefinition = "TEXT")
    private String razonRechazo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rechazada_por_id")
    private Usuario rechazadaPor;

    @Column(name = "total_valoraciones")
    @Builder.Default
    private Integer totalValoraciones = 0;

    @Column(name = "total_reservas")
    @Builder.Default
    private Integer totalReservas = 0;

    @Column(name = "verificacion_completa")
    @Builder.Default
    private Boolean verificacionCompleta = false;

    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Valoracion> valoraciones = new HashSet<>();

    @OneToMany(mappedBy = "propiedad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Documento> documentos = new HashSet<>();

    // Métodos auxiliares
    public boolean estaActiva() {
        return estado == EstadoPropiedad.ACTIVA;
    }

    public boolean estaDisponible() {
        return estado == EstadoPropiedad.ACTIVA && verificacionCompleta;
    }

    public void agregarImagen(String urlImagen) {
        this.imagenes.add(urlImagen);
    }

    public void agregarServicio(String servicio) {
        this.servicios.add(servicio);
    }
}
