package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Documento;
import com.staykonnect.domain.enums.EstadoVerificacion;
import com.staykonnect.domain.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad Documento
 */
@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByPropiedadId(Long propiedadId);

    List<Documento> findByTipo(TipoDocumento tipo);

    List<Documento> findByEstadoVerificacion(EstadoVerificacion estado);

    @Query("SELECT d FROM Documento d WHERE d.propiedad.id = :propiedadId " +
           "AND d.estadoVerificacion = :estado")
    List<Documento> findByPropiedadIdAndEstado(@Param("propiedadId") Long propiedadId,
                                                 @Param("estado") EstadoVerificacion estado);

    @Query("SELECT d FROM Documento d WHERE d.estadoVerificacion IN ('PENDIENTE', 'EN_REVISION') " +
           "ORDER BY d.createdAt ASC")
    List<Documento> findDocumentosPendientesVerificacion();

    @Query("SELECT d FROM Documento d WHERE d.fechaVencimiento <= :fecha " +
           "AND d.estadoVerificacion = 'APROBADO'")
    List<Documento> findDocumentosProximosAVencer(@Param("fecha") LocalDate fecha);

    @Query("SELECT d FROM Documento d WHERE d.fechaVencimiento < CURRENT_DATE " +
           "AND d.estadoVerificacion = 'APROBADO'")
    List<Documento> findDocumentosVencidos();

    @Query("SELECT COUNT(d) > 0 FROM Documento d WHERE d.propiedad.id = :propiedadId " +
           "AND d.estadoVerificacion = 'APROBADO' AND d.tipo = 'LICENCIA_OPERACION'")
    boolean propiedadTieneLicenciaAprobada(@Param("propiedadId") Long propiedadId);

    @Query("SELECT COUNT(d) FROM Documento d WHERE d.propiedad.id = :propiedadId " +
           "AND d.estadoVerificacion = 'APROBADO'")
    long countDocumentosAprobadosByPropiedad(@Param("propiedadId") Long propiedadId);
}
