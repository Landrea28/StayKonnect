package com.staykonnect.domain.entity;

import com.staykonnect.domain.enums.EstadoCuenta;
import com.staykonnect.domain.enums.RolUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Usuario - Representa a todos los usuarios del sistema
 * (Viajeros, Anfitriones y Administradores)
 */
@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 50)
    @Column(nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email debe ser válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RolUsuario rol = RolUsuario.VIAJERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EstadoCuenta estado = EstadoCuenta.PENDIENTE_VERIFICACION;

    @Column(name = "email_verificado")
    @Builder.Default
    private Boolean emailVerificado = false;

    @Column(name = "token_verificacion")
    private String tokenVerificacion;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    // Campos de administración
    @Column(name = "baneado")
    @Builder.Default
    private Boolean baneado = false;

    @Column(name = "fecha_baneo")
    private LocalDateTime fechaBaneo;

    @Column(name = "razon_baneo", columnDefinition = "TEXT")
    private String razonBaneo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baneado_por_id")
    private Usuario baneadoPor;

    // Campos de reputación
    @Column(name = "puntuacion_promedio")
    @Builder.Default
    private Double puntuacionPromedio = 0.0;

    @Column(name = "total_valoraciones")
    @Builder.Default
    private Integer totalValoraciones = 0;

    // Relaciones
    @OneToMany(mappedBy = "anfitrion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Propiedad> propiedades = new HashSet<>();

    @OneToMany(mappedBy = "viajero", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "remitente", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Mensaje> mensajesEnviados = new HashSet<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Mensaje> mensajesRecibidos = new HashSet<>();

    @OneToMany(mappedBy = "evaluador", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Valoracion> valoracionesRealizadas = new HashSet<>();

    @OneToMany(mappedBy = "evaluado", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Valoracion> valoracionesRecibidas = new HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Notificacion> notificaciones = new HashSet<>();

    // Métodos auxiliares
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public boolean esAnfitrion() {
        return rol == RolUsuario.ANFITRION;
    }

    public boolean esViajero() {
        return rol == RolUsuario.VIAJERO;
    }

    public boolean esAdmin() {
        return rol == RolUsuario.ADMIN;
    }

    public boolean estaCuentaActiva() {
        return estado == EstadoCuenta.ACTIVA && emailVerificado;
    }
}
