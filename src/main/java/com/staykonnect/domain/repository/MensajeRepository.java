package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    /**
     * Obtener mensajes entre dos usuarios (conversación bidireccional)
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE (m.remitente.id = :usuario1Id AND m.destinatario.id = :usuario2Id)
           OR (m.remitente.id = :usuario2Id AND m.destinatario.id = :usuario1Id)
        ORDER BY m.createdDate DESC
    """)
    Page<Mensaje> findConversacionEntreUsuarios(
        @Param("usuario1Id") Long usuario1Id,
        @Param("usuario2Id") Long usuario2Id,
        Pageable pageable
    );

    /**
     * Obtener todas las conversaciones de un usuario (últimos mensajes por usuario)
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.id IN (
            SELECT MAX(m2.id) FROM Mensaje m2
            WHERE m2.remitente.id = :usuarioId OR m2.destinatario.id = :usuarioId
            GROUP BY CASE
                WHEN m2.remitente.id = :usuarioId THEN m2.destinatario.id
                ELSE m2.remitente.id
            END
        )
        ORDER BY m.createdDate DESC
    """)
    Page<Mensaje> findUltimosMensajesPorConversacion(
        @Param("usuarioId") Long usuarioId,
        Pageable pageable
    );

    /**
     * Contar mensajes no leídos de un usuario
     */
    @Query("""
        SELECT COUNT(m) FROM Mensaje m
        WHERE m.destinatario.id = :usuarioId
        AND m.leido = false
    """)
    Long countMensajesNoLeidos(@Param("usuarioId") Long usuarioId);

    /**
     * Contar mensajes no leídos entre dos usuarios
     */
    @Query("""
        SELECT COUNT(m) FROM Mensaje m
        WHERE m.remitente.id = :remitenteId
        AND m.destinatario.id = :destinatarioId
        AND m.leido = false
    """)
    Long countMensajesNoLeidosEntreUsuarios(
        @Param("remitenteId") Long remitenteId,
        @Param("destinatarioId") Long destinatarioId
    );

    /**
     * Obtener mensajes no leídos de un usuario
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.destinatario.id = :usuarioId
        AND m.leido = false
        ORDER BY m.createdDate DESC
    """)
    List<Mensaje> findMensajesNoLeidos(@Param("usuarioId") Long usuarioId);

    /**
     * Marcar todos los mensajes de una conversación como leídos
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.remitente.id = :remitenteId
        AND m.destinatario.id = :destinatarioId
        AND m.leido = false
    """)
    List<Mensaje> findMensajesNoLeidosDeRemitente(
        @Param("remitenteId") Long remitenteId,
        @Param("destinatarioId") Long destinatarioId
    );

    /**
     * Obtener mensajes relacionados a una reserva
     */
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.reserva.id = :reservaId
        ORDER BY m.createdDate ASC
    """)
    List<Mensaje> findByReservaId(@Param("reservaId") Long reservaId);

    /**
     * Verificar si existe conversación entre dos usuarios
     */
    @Query("""
        SELECT COUNT(m) > 0 FROM Mensaje m
        WHERE (m.remitente.id = :usuario1Id AND m.destinatario.id = :usuario2Id)
           OR (m.remitente.id = :usuario2Id AND m.destinatario.id = :usuario1Id)
    """)
    boolean existeConversacionEntreUsuarios(
        @Param("usuario1Id") Long usuario1Id,
        @Param("usuario2Id") Long usuario2Id
    );

    // Métodos para administración
    Long countByCreatedDateAfter(java.time.LocalDateTime fecha);
    
    Long countByRemitenteId(Long usuarioId);
    
    @Query("SELECT COUNT(DISTINCT CASE WHEN m.remitente.id < m.destinatario.id THEN CONCAT(m.remitente.id, '-', m.destinatario.id) ELSE CONCAT(m.destinatario.id, '-', m.remitente.id) END) FROM Mensaje m")
    Long countConversacionesActivas();
}
