package com.staykonnect.specification;

import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.EstadoReserva;
import com.staykonnect.domain.enums.TipoPropiedad;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specifications para búsqueda dinámica de propiedades.
 * Utiliza JPA Criteria API para construir queries dinámicas.
 */
public class PropiedadSpecification {

    /**
     * Filtra solo propiedades activas.
     */
    public static Specification<Propiedad> esActiva() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("estado"), EstadoPropiedad.ACTIVA);
    }

    /**
     * Búsqueda de texto en título y descripción.
     */
    public static Specification<Propiedad> conTexto(String texto) {
        return (root, query, criteriaBuilder) -> {
            if (texto == null || texto.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + texto.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("titulo")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("descripcion")), pattern)
            );
        };
    }

    /**
     * Filtra por ciudad (case-insensitive).
     */
    public static Specification<Propiedad> enCiudad(String ciudad) {
        return (root, query, criteriaBuilder) -> {
            if (ciudad == null || ciudad.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ciudad")),
                    "%" + ciudad.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filtra por país (case-insensitive).
     */
    public static Specification<Propiedad> enPais(String pais) {
        return (root, query, criteriaBuilder) -> {
            if (pais == null || pais.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("pais")),
                    "%" + pais.toLowerCase() + "%"
            );
        };
    }

    /**
     * Filtra por tipo de propiedad.
     */
    public static Specification<Propiedad> deTipo(TipoPropiedad tipo) {
        return (root, query, criteriaBuilder) -> {
            if (tipo == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("tipoPropiedad"), tipo);
        };
    }

    /**
     * Filtra por capacidad mínima.
     */
    public static Specification<Propiedad> conCapacidadMinima(Integer capacidad) {
        return (root, query, criteriaBuilder) -> {
            if (capacidad == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("capacidad"), capacidad);
        };
    }

    /**
     * Filtra por número mínimo de habitaciones.
     */
    public static Specification<Propiedad> conHabitacionesMinimas(Integer habitaciones) {
        return (root, query, criteriaBuilder) -> {
            if (habitaciones == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("habitaciones"), habitaciones);
        };
    }

    /**
     * Filtra por número mínimo de camas.
     */
    public static Specification<Propiedad> conCamasMinimas(Integer camas) {
        return (root, query, criteriaBuilder) -> {
            if (camas == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("camas"), camas);
        };
    }

    /**
     * Filtra por número mínimo de baños.
     */
    public static Specification<Propiedad> conBanosMinimos(Integer banos) {
        return (root, query, criteriaBuilder) -> {
            if (banos == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("banos"), banos);
        };
    }

    /**
     * Filtra por rango de precio.
     */
    public static Specification<Propiedad> enRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (precioMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("precioPorNoche"), precioMin));
            }
            
            if (precioMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("precioPorNoche"), precioMax));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filtra propiedades que tengan TODOS los servicios especificados.
     */
    public static Specification<Propiedad> conServicios(List<String> servicios) {
        return (root, query, criteriaBuilder) -> {
            if (servicios == null || servicios.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            // Para cada servicio requerido, verificar que exista en la colección
            List<Predicate> predicates = new ArrayList<>();
            for (String servicio : servicios) {
                Expression<List<String>> serviciosPropiedad = root.get("servicios");
                predicates.add(criteriaBuilder.isMember(servicio, serviciosPropiedad));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filtra por puntuación mínima.
     */
    public static Specification<Propiedad> conPuntuacionMinima(Double puntuacion) {
        return (root, query, criteriaBuilder) -> {
            if (puntuacion == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("puntuacionPromedio"), puntuacion);
        };
    }

    /**
     * Filtra propiedades disponibles en un rango de fechas.
     * Una propiedad está disponible si NO tiene reservas confirmadas/pagadas/en curso en ese rango.
     */
    public static Specification<Propiedad> disponibleEntre(LocalDate fechaInicio, LocalDate fechaFin) {
        return (root, query, criteriaBuilder) -> {
            if (fechaInicio == null || fechaFin == null) {
                return criteriaBuilder.conjunction();
            }

            // Subquery para encontrar propiedades con reservas que se solapan
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Reserva> reservaRoot = subquery.from(Reserva.class);
            
            subquery.select(reservaRoot.get("propiedad").get("id"))
                    .where(
                            criteriaBuilder.and(
                                    // Reserva está confirmada, pagada o en curso
                                    reservaRoot.get("estado").in(
                                            EstadoReserva.CONFIRMADA,
                                            EstadoReserva.PAGADA,
                                            EstadoReserva.EN_CURSO
                                    ),
                                    // Las fechas se solapan
                                    criteriaBuilder.or(
                                            // La reserva empieza durante el periodo buscado
                                            criteriaBuilder.between(
                                                    reservaRoot.get("fechaCheckin"),
                                                    fechaInicio,
                                                    fechaFin
                                            ),
                                            // La reserva termina durante el periodo buscado
                                            criteriaBuilder.between(
                                                    reservaRoot.get("fechaCheckout"),
                                                    fechaInicio,
                                                    fechaFin
                                            ),
                                            // La reserva contiene completamente el periodo buscado
                                            criteriaBuilder.and(
                                                    criteriaBuilder.lessThanOrEqualTo(
                                                            reservaRoot.get("fechaCheckin"),
                                                            fechaInicio
                                                    ),
                                                    criteriaBuilder.greaterThanOrEqualTo(
                                                            reservaRoot.get("fechaCheckout"),
                                                            fechaFin
                                                    )
                                            )
                                    )
                            )
                    );

            // Retornar propiedades que NO están en el subquery
            return criteriaBuilder.not(root.get("id").in(subquery));
        };
    }
}
