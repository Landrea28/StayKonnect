package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Ticket;
import com.staykonnect.domain.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Ticket
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCodigoTicket(String codigoTicket);

    List<Ticket> findByUsuarioId(Long usuarioId);

    List<Ticket> findByEstado(EstadoTicket estado);

    List<Ticket> findByReservaId(Long reservaId);

    @Query("SELECT t FROM Ticket t WHERE t.estado IN ('ABIERTO', 'ASIGNADO', 'EN_PROCESO') " +
           "ORDER BY CASE t.prioridad " +
           "WHEN 'URGENTE' THEN 1 " +
           "WHEN 'ALTA' THEN 2 " +
           "WHEN 'MEDIA' THEN 3 " +
           "WHEN 'BAJA' THEN 4 END, t.createdAt ASC")
    List<Ticket> findTicketsAbiertosOrdenadosPorPrioridad();

    @Query("SELECT t FROM Ticket t WHERE t.usuario.id = :usuarioId " +
           "AND t.estado IN ('ABIERTO', 'ASIGNADO', 'EN_PROCESO')")
    List<Ticket> findTicketsAbiertosByUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t FROM Ticket t WHERE t.prioridad = :prioridad AND t.estado <> 'CERRADO' " +
           "ORDER BY t.createdAt ASC")
    List<Ticket> findByPrioridad(@Param("prioridad") String prioridad);

    @Query("SELECT t FROM Ticket t WHERE t.categoria = :categoria ORDER BY t.createdAt DESC")
    List<Ticket> findByCategoria(@Param("categoria") String categoria);

    long countByEstado(EstadoTicket estado);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.estado IN ('ABIERTO', 'ASIGNADO', 'EN_PROCESO')")
    long countTicketsAbiertos();
}
