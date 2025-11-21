package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.TipoPropiedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad Propiedad
 * Incluye JpaSpecificationExecutor para búsquedas complejas con filtros
 */
@Repository
public interface PropiedadRepository extends JpaRepository<Propiedad, Long>, JpaSpecificationExecutor<Propiedad> {

    List<Propiedad> findByAnfitrionId(Long anfitrionId);

    List<Propiedad> findByEstado(EstadoPropiedad estado);

    List<Propiedad> findByTipoPropiedad(TipoPropiedad tipo);

    @Query("SELECT p FROM Propiedad p WHERE p.estado = 'ACTIVA' AND p.verificacionCompleta = true")
    List<Propiedad> findPropiedadesDisponibles();

    @Query("SELECT p FROM Propiedad p WHERE p.ciudad = :ciudad AND p.estado = 'ACTIVA'")
    List<Propiedad> findByCiudad(@Param("ciudad") String ciudad);

    @Query("SELECT p FROM Propiedad p WHERE p.precioPorNoche BETWEEN :minPrecio AND :maxPrecio AND p.estado = 'ACTIVA'")
    List<Propiedad> findByRangoPrecio(@Param("minPrecio") BigDecimal minPrecio, @Param("maxPrecio") BigDecimal maxPrecio);

    @Query("SELECT p FROM Propiedad p WHERE p.capacidad >= :capacidad AND p.estado = 'ACTIVA'")
    List<Propiedad> findByCapacidadMinima(@Param("capacidad") Integer capacidad);

    @Query("SELECT p FROM Propiedad p WHERE p.puntuacionPromedio >= :minPuntuacion AND p.estado = 'ACTIVA' ORDER BY p.puntuacionPromedio DESC")
    List<Propiedad> findPropiedadesConMejorPuntuacion(@Param("minPuntuacion") Double minPuntuacion);

    @Query("SELECT p FROM Propiedad p WHERE p.anfitrion.id = :anfitrionId AND p.estado <> 'ELIMINADA'")
    List<Propiedad> findPropiedadesByAnfitrion(@Param("anfitrionId") Long anfitrionId);

    long countByAnfitrionIdAndEstado(Long anfitrionId, EstadoPropiedad estado);

    // Métodos para administración
    Long countByAnfitrionId(Long anfitrionId);
    
    Long countByAprobadaTrue();
    
    Long countByAprobadaFalseAndRechazadaFalse();
    
    Long countByRechazadaTrue();
    
    Page<Propiedad> findByAprobadaFalseAndRechazadaFalse(Pageable pageable);
}
