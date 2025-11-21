package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.entity.enums.EstadoReserva;
import com.staykonnect.domain.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Reserva
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    List<Reserva> findByViajeroId(Long viajeroId);

    List<Reserva> findByPropiedadId(Long propiedadId);

    List<Reserva> findByEstado(EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.propiedad.anfitrion.id = :anfitrionId")
    List<Reserva> findByAnfitrionId(@Param("anfitrionId") Long anfitrionId);

    @Query("SELECT r FROM Reserva r WHERE r.viajero.id = :viajeroId AND r.estado = :estado")
    List<Reserva> findByViajeroIdAndEstado(@Param("viajeroId") Long viajeroId, @Param("estado") EstadoReserva estado);

    @Query("SELECT r FROM Reserva r WHERE r.propiedad.id = :propiedadId AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO')")
    List<Reserva> findReservasActivasByPropiedad(@Param("propiedadId") Long propiedadId);

    // Verificar disponibilidad de propiedad en un rango de fechas
    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.propiedad.id = :propiedadId " +
           "AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO') " +
           "AND ((r.fechaCheckin <= :fechaCheckout AND r.fechaCheckout >= :fechaCheckin))")
    boolean existeReservaEnRangoFechas(@Param("propiedadId") Long propiedadId,
                                        @Param("fechaCheckin") LocalDate fechaCheckin,
                                        @Param("fechaCheckout") LocalDate fechaCheckout);

    @Query("SELECT r FROM Reserva r WHERE r.fechaCheckin = :fecha AND r.estado = 'PAGADA'")
    List<Reserva> findReservasConCheckinHoy(@Param("fecha") LocalDate fecha);

    @Query("SELECT r FROM Reserva r WHERE r.fechaCheckout = :fecha AND r.estado = 'EN_CURSO'")
    List<Reserva> findReservasConCheckoutHoy(@Param("fecha") LocalDate fecha);

    // Queries adicionales para el servicio de reservas
    @Query("SELECT r FROM Reserva r WHERE r.viajero.id = :viajeroId ORDER BY r.createdDate DESC")
    org.springframework.data.domain.Page<Reserva> findByViajeroIdOrderByCreatedDateDesc(
            @Param("viajeroId") Long viajeroId, 
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.propiedad.anfitrion.id = :anfitrionId ORDER BY r.createdDate DESC")
    org.springframework.data.domain.Page<Reserva> findByPropiedadAnfitrionIdOrderByCreatedDateDesc(
            @Param("anfitrionId") Long anfitrionId, 
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT r FROM Reserva r WHERE r.propiedad.id = :propiedadId ORDER BY r.fechaCheckin DESC")
    org.springframework.data.domain.Page<Reserva> findByPropiedadIdOrderByFechaCheckinDesc(
            @Param("propiedadId") Long propiedadId, 
            org.springframework.data.domain.Pageable pageable);

    // Buscar reservas conflictivas para validar disponibilidad
    @Query("SELECT r FROM Reserva r WHERE r.propiedad.id = :propiedadId " +
           "AND r.estado IN :estados " +
           "AND ((r.fechaCheckin < :fechaCheckout AND r.fechaCheckout > :fechaCheckin))")
    List<Reserva> findReservasConflictivas(
            @Param("propiedadId") Long propiedadId,
            @Param("fechaCheckin") LocalDate fechaCheckin,
            @Param("fechaCheckout") LocalDate fechaCheckout,
            @Param("estados") List<EstadoReserva> estados);

    long countByViajeroIdAndEstado(Long viajeroId, EstadoReserva estado);

    long countByPropiedadIdAndEstado(Long propiedadId, EstadoReserva estado);

    // Métodos para administración
    Long countByEstado(EstadoReserva estado);
    
    Page<Reserva> findByEstado(EstadoReserva estado, Pageable pageable);
    
    Long countByViajeroId(Long viajeroId);
    
    Long countByPropiedadAnfitrionId(Long anfitrionId);
    
    Long countByViajeroAndEstadoIn(Usuario viajero, List<EstadoReserva> estados);
    
    Long countByPropiedadId(Long propiedadId);
    
    @Query("SELECT SUM(r.precioTotal) FROM Reserva r WHERE r.propiedad.id = :propiedadId AND r.estado = :estado")
    BigDecimal sumPrecioTotalByPropiedadIdAndEstado(@Param("propiedadId") Long propiedadId, @Param("estado") EstadoReserva estado);

    // Queries para reportes avanzados
    
    // Ingresos por período
    @Query(value = "SELECT DATE_TRUNC(:periodo, r.created_date) as fecha, " +
                   "COUNT(*) as numero_reservas, " +
                   "SUM(r.precio_total) as ingresos_brutos, " +
                   "SUM(r.comision) as comisiones, " +
                   "SUM(r.precio_total - r.comision) as ingresos_netos " +
                   "FROM reserva r " +
                   "WHERE r.created_date BETWEEN :fechaInicio AND :fechaFin " +
                   "AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "GROUP BY DATE_TRUNC(:periodo, r.created_date) " +
                   "ORDER BY fecha", 
           nativeQuery = true)
    List<Object[]> obtenerIngresosPorPeriodo(@Param("periodo") String periodo,
                                              @Param("fechaInicio") LocalDate fechaInicio,
                                              @Param("fechaFin") LocalDate fechaFin);
    
    // Ocupación por propiedad
    @Query(value = "SELECT p.id, p.nombre, p.ciudad, p.tipo, " +
                   "COUNT(r.id) as numero_reservas, " +
                   "SUM(r.precio_total) as ingresos, " +
                   "COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio, " +
                   "SUM(DATE_PART('day', r.fecha_checkout - r.fecha_checkin)) as dias_reservados " +
                   "FROM propiedad p " +
                   "LEFT JOIN reserva r ON p.id = r.propiedad_id " +
                   "    AND r.created_date BETWEEN :fechaInicio AND :fechaFin " +
                   "    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "LEFT JOIN valoracion v ON p.id = v.propiedad_id " +
                   "WHERE p.visible = true " +
                   "GROUP BY p.id, p.nombre, p.ciudad, p.tipo " +
                   "ORDER BY ingresos DESC",
           nativeQuery = true)
    List<Object[]> obtenerOcupacionPorPropiedad(@Param("fechaInicio") LocalDate fechaInicio,
                                                 @Param("fechaFin") LocalDate fechaFin);
    
    // Top propiedades por ingresos
    @Query(value = "SELECT p.id, p.nombre, p.ciudad, p.tipo, " +
                   "u.id as anfitrion_id, CONCAT(u.nombre, ' ', u.apellido) as anfitrion_nombre, " +
                   "COUNT(r.id) as numero_reservas, " +
                   "SUM(r.precio_total) as ingresos, " +
                   "COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio, " +
                   "COUNT(DISTINCT v.id) as numero_valoraciones, " +
                   "AVG(r.precio_total / NULLIF(DATE_PART('day', r.fecha_checkout - r.fecha_checkin), 0)) as precio_promedio_noche " +
                   "FROM propiedad p " +
                   "INNER JOIN usuario u ON p.anfitrion_id = u.id " +
                   "LEFT JOIN reserva r ON p.id = r.propiedad_id " +
                   "    AND r.created_date BETWEEN :fechaInicio AND :fechaFin " +
                   "    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "LEFT JOIN valoracion v ON p.id = v.propiedad_id " +
                   "WHERE p.visible = true " +
                   "GROUP BY p.id, p.nombre, p.ciudad, p.tipo, u.id, u.nombre, u.apellido " +
                   "ORDER BY ingresos DESC " +
                   "LIMIT :limite",
           nativeQuery = true)
    List<Object[]> obtenerTopPropiedadesPorIngresos(@Param("fechaInicio") LocalDate fechaInicio,
                                                     @Param("fechaFin") LocalDate fechaFin,
                                                     @Param("limite") int limite);
    
    // Top anfitriones
    @Query(value = "SELECT u.id, CONCAT(u.nombre, ' ', u.apellido) as nombre, u.email, " +
                   "COUNT(DISTINCT p.id) as numero_propiedades, " +
                   "COUNT(DISTINCT CASE WHEN p.visible = true THEN p.id END) as propiedades_activas, " +
                   "COUNT(r.id) as numero_reservas, " +
                   "COUNT(CASE WHEN r.estado = 'COMPLETADA' THEN 1 END) as reservas_completadas, " +
                   "SUM(r.precio_total) as ingresos, " +
                   "COALESCE(AVG(v.puntuacion), 0) as puntuacion_promedio, " +
                   "COUNT(DISTINCT v.id) as numero_valoraciones " +
                   "FROM usuario u " +
                   "INNER JOIN propiedad p ON u.id = p.anfitrion_id " +
                   "LEFT JOIN reserva r ON p.id = r.propiedad_id " +
                   "    AND r.created_date BETWEEN :fechaInicio AND :fechaFin " +
                   "    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "LEFT JOIN valoracion v ON u.id = v.valorado_id " +
                   "WHERE u.rol = 'ANFITRION' " +
                   "GROUP BY u.id, u.nombre, u.apellido, u.email " +
                   "ORDER BY ingresos DESC " +
                   "LIMIT :limite",
           nativeQuery = true)
    List<Object[]> obtenerTopAnfitriones(@Param("fechaInicio") LocalDate fechaInicio,
                                         @Param("fechaFin") LocalDate fechaFin,
                                         @Param("limite") int limite);
    
    // Estacionalidad por mes
    @Query(value = "SELECT EXTRACT(MONTH FROM r.fecha_checkin) as mes, " +
                   "COUNT(r.id) as numero_reservas, " +
                   "SUM(r.precio_total) as ingresos, " +
                   "AVG(r.precio_total / NULLIF(DATE_PART('day', r.fecha_checkout - r.fecha_checkin), 0)) as precio_promedio_noche, " +
                   "COUNT(DISTINCT r.viajero_id) as numero_viajeros " +
                   "FROM reserva r " +
                   "WHERE EXTRACT(YEAR FROM r.fecha_checkin) = :anio " +
                   "AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "GROUP BY EXTRACT(MONTH FROM r.fecha_checkin) " +
                   "ORDER BY mes",
           nativeQuery = true)
    List<Object[]> obtenerEstacionalidadPorMes(@Param("anio") int anio);
    
    // Comisiones por período
    @Query(value = "SELECT DATE_TRUNC(:periodo, r.created_date) as fecha, " +
                   "COUNT(*) as numero_transacciones, " +
                   "SUM(r.comision) as comisiones_generadas, " +
                   "SUM(r.precio_total) as ingresos_totales, " +
                   "AVG(r.comision) as comision_promedio " +
                   "FROM reserva r " +
                   "WHERE r.created_date BETWEEN :fechaInicio AND :fechaFin " +
                   "AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA') " +
                   "GROUP BY DATE_TRUNC(:periodo, r.created_date) " +
                   "ORDER BY fecha",
           nativeQuery = true)
    List<Object[]> obtenerComisionesPorPeriodo(@Param("periodo") String periodo,
                                                @Param("fechaInicio") LocalDate fechaInicio,
                                                @Param("fechaFin") LocalDate fechaFin);
    
    // Tasa de ocupación promedio global
    @Query(value = "WITH dias_totales AS (" +
                   "    SELECT SUM(DATE_PART('day', :fechaFin - :fechaInicio)) * COUNT(DISTINCT p.id) as total " +
                   "    FROM propiedad p WHERE p.visible = true" +
                   "), " +
                   "dias_reservados AS (" +
                   "    SELECT SUM(DATE_PART('day', r.fecha_checkout - r.fecha_checkin)) as total " +
                   "    FROM reserva r " +
                   "    WHERE r.fecha_checkin BETWEEN :fechaInicio AND :fechaFin " +
                   "    AND r.estado IN ('CONFIRMADA', 'PAGADA', 'EN_CURSO', 'COMPLETADA')" +
                   ") " +
                   "SELECT COALESCE((dr.total / NULLIF(dt.total, 0)) * 100, 0) " +
                   "FROM dias_totales dt, dias_reservados dr",
           nativeQuery = true)
    BigDecimal calcularTasaOcupacionGlobal(@Param("fechaInicio") LocalDate fechaInicio,
                                           @Param("fechaFin") LocalDate fechaFin);
}
