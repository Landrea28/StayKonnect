package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Notificacion;
import com.staykonnect.domain.enums.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Notificación
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioId(Long usuarioId);

    List<Notificacion> findByTipo(TipoNotificacion tipo);

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false " +
           "ORDER BY n.createdAt DESC")
    List<Notificacion> findNotificacionesNoLeidas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false")
    long countNotificacionesNoLeidas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId " +
           "ORDER BY n.createdAt DESC")
    List<Notificacion> findNotificacionesByUsuarioOrdenadas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.tipo = :tipo " +
           "ORDER BY n.createdAt DESC")
    List<Notificacion> findByUsuarioIdAndTipo(@Param("usuarioId") Long usuarioId,
                                                @Param("tipo") TipoNotificacion tipo);
    
    // Paginación
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId " +
           "ORDER BY n.leida ASC, n.createdAt DESC")
    org.springframework.data.domain.Page<Notificacion> findByUsuarioIdOrderByLeidaAndFecha(
        @Param("usuarioId") Long usuarioId,
        org.springframework.data.domain.Pageable pageable);
    
    // Marcar múltiples como leídas
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLectura = CURRENT_TIMESTAMP " +
           "WHERE n.usuario.id = :usuarioId AND n.leida = false")
    @org.springframework.data.jpa.repository.Modifying
    void marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
    
    // Eliminar notificaciones antiguas
    @Query("DELETE FROM Notificacion n WHERE n.createdAt < :fecha AND n.leida = true")
    @org.springframework.data.jpa.repository.Modifying
    void eliminarNotificacionesAntiguas(@Param("fecha") java.time.LocalDateTime fecha);
    
    // Notificaciones por rango de fechas
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId " +
           "AND n.createdAt BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY n.createdAt DESC")
    List<Notificacion> findByUsuarioIdAndFechas(
        @Param("usuarioId") Long usuarioId,
        @Param("fechaInicio") java.time.LocalDateTime fechaInicio,
        @Param("fechaFin") java.time.LocalDateTime fechaFin);
    
    // Estadísticas por tipo
    @Query("SELECT n.tipo, COUNT(n) FROM Notificacion n " +
           "WHERE n.usuario.id = :usuarioId " +
           "GROUP BY n.tipo")
    List<Object[]> obtenerEstadisticasPorTipo(@Param("usuarioId") Long usuarioId);
    
    // Contar total por usuario
    long countByUsuarioId(Long usuarioId);
}
