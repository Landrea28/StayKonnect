package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Pago;
import com.staykonnect.domain.entity.enums.EstadoPago;
import com.staykonnect.domain.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Pago
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByReservaId(Long reservaId);

    Optional<Pago> findByIdTransaccionExterna(String idTransaccion);

    Optional<Pago> findByTransaccionId(String transaccionId);

    Optional<Pago> findByReservaIdAndEstado(Long reservaId, EstadoPago estado);

    boolean existsByReservaIdAndEstado(Long reservaId, EstadoPago estado);

    List<Pago> findByEstado(EstadoPago estado);

    @Query("SELECT p FROM Pago p WHERE p.estado = 'RETENIDO' " +
           "AND p.fechaRetencion <= :fechaLimite")
    List<Pago> findPagosRetenidosParaLiberar(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT p FROM Pago p WHERE p.reserva.propiedad.anfitrion.id = :anfitrionId " +
           "AND p.estado = 'LIBERADO'")
    List<Pago> findPagosLiberadosByAnfitrion(@Param("anfitrionId") Long anfitrionId);

    @Query("SELECT SUM(p.montoAnfitrion) FROM Pago p WHERE p.reserva.propiedad.anfitrion.id = :anfitrionId " +
           "AND p.estado = 'LIBERADO'")
    Double calcularIngresosAnfitrion(@Param("anfitrionId") Long anfitrionId);

    @Query("SELECT SUM(p.comisionPlataforma) FROM Pago p WHERE p.estado = 'LIBERADO' " +
           "AND p.fechaLiberacion BETWEEN :fechaInicio AND :fechaFin")
    Double calcularComisionesPlataforma(@Param("fechaInicio") LocalDateTime fechaInicio,
                                         @Param("fechaFin") LocalDateTime fechaFin);

    long countByEstado(EstadoPago estado);

    // Métodos para administración
    Long countByEstado(EstadoPago estado);
    
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = :estado")
    BigDecimal sumByEstado(@Param("estado") EstadoPago estado);
    
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = :estado AND p.createdDate >= :fecha")
    BigDecimal sumByEstadoAndFechaAfter(@Param("estado") EstadoPago estado, @Param("fecha") LocalDateTime fecha);
}
