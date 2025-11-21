package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Valoracion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    /**
     * Verificar si existe valoración para una reserva
     */
    boolean existsByReservaId(Long reservaId);

    /**
     * Obtener valoración por reserva
     */
    Optional<Valoracion> findByReservaId(Long reservaId);

    /**
     * Obtener valoraciones de una propiedad (visibles)
     */
    @Query("""
        SELECT v FROM Valoracion v
        WHERE v.propiedad.id = :propiedadId
        AND v.visible = true
        ORDER BY v.createdDate DESC
    """)
    Page<Valoracion> findByPropiedadIdAndVisibleTrue(
        @Param("propiedadId") Long propiedadId,
        Pageable pageable
    );

    /**
     * Obtener valoraciones recibidas por un anfitrión (visibles)
     */
    @Query("""
        SELECT v FROM Valoracion v
        WHERE v.valorado.id = :anfitrionId
        AND v.visible = true
        ORDER BY v.createdDate DESC
    """)
    Page<Valoracion> findByValoradoIdAndVisibleTrue(
        @Param("anfitrionId") Long anfitrionId,
        Pageable pageable
    );

    /**
     * Obtener valoraciones realizadas por un usuario
     */
    @Query("""
        SELECT v FROM Valoracion v
        WHERE v.valorador.id = :usuarioId
        ORDER BY v.createdDate DESC
    """)
    Page<Valoracion> findByValoradorId(
        @Param("usuarioId") Long usuarioId,
        Pageable pageable
    );

    /**
     * Calcular puntuación promedio de una propiedad
     */
    @Query("""
        SELECT AVG(v.puntuacion)
        FROM Valoracion v
        WHERE v.propiedad.id = :propiedadId
        AND v.visible = true
    """)
    Double calcularPromedioPropiedad(@Param("propiedadId") Long propiedadId);

    /**
     * Calcular puntuación promedio de un anfitrión
     */
    @Query("""
        SELECT AVG(v.puntuacion)
        FROM Valoracion v
        WHERE v.valorado.id = :anfitrionId
        AND v.visible = true
    """)
    Double calcularPromedioAnfitrion(@Param("anfitrionId") Long anfitrionId);

    /**
     * Contar valoraciones de una propiedad
     */
    @Query("""
        SELECT COUNT(v)
        FROM Valoracion v
        WHERE v.propiedad.id = :propiedadId
        AND v.visible = true
    """)
    Long contarValoracionesPropiedad(@Param("propiedadId") Long propiedadId);

    /**
     * Contar valoraciones de un anfitrión
     */
    @Query("""
        SELECT COUNT(v)
        FROM Valoracion v
        WHERE v.valorado.id = :anfitrionId
        AND v.visible = true
    """)
    Long contarValoracionesAnfitrion(@Param("anfitrionId") Long anfitrionId);

    /**
     * Obtener valoraciones pendientes de respuesta del anfitrión
     */
    @Query("""
        SELECT v FROM Valoracion v
        WHERE v.valorado.id = :anfitrionId
        AND v.respuestaAnfitrion IS NULL
        AND v.visible = true
        ORDER BY v.createdDate DESC
    """)
    Page<Valoracion> findValoracionesPendientesRespuesta(
        @Param("anfitrionId") Long anfitrionId,
        Pageable pageable
    );

    /**
     * Obtener distribución de puntuaciones de una propiedad
     */
    @Query("""
        SELECT v.puntuacion as puntuacion, COUNT(v) as cantidad
        FROM Valoracion v
        WHERE v.propiedad.id = :propiedadId
        AND v.visible = true
        GROUP BY v.puntuacion
        ORDER BY v.puntuacion DESC
    """)
    Object[][] obtenerDistribucionPuntuaciones(@Param("propiedadId") Long propiedadId);

    /**
     * Obtener valoraciones recientes (últimas 10)
     */
    @Query("""
        SELECT v FROM Valoracion v
        WHERE v.visible = true
        ORDER BY v.createdDate DESC
        LIMIT 10
    """)
    Page<Valoracion> findValoracionesRecientes(Pageable pageable);

    // Métodos para administración
    Long countByVisibleFalse();
    
    Long countByValoradoId(Long usuarioId);
    
    Long countByPropiedadId(Long propiedadId);
    
    @Query("SELECT AVG(v.puntuacion) FROM Valoracion v WHERE v.visible = true")
    Double calcularPromedioGeneral();
}
