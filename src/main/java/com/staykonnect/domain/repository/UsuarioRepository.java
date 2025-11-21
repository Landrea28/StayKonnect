package com.staykonnect.domain.repository;

import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.entity.enums.Rol;
import com.staykonnect.domain.enums.EstadoCuenta;
import com.staykonnect.domain.enums.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRol(RolUsuario rol);

    List<Usuario> findByEstado(EstadoCuenta estado);

    Optional<Usuario> findByTokenVerificacion(String token);

    Optional<Usuario> findByTokenRecuperacion(String token);

    @Query("SELECT u FROM Usuario u WHERE u.emailVerificado = true AND u.estado = 'ACTIVA'")
    List<Usuario> findUsuariosActivos();

    @Query("SELECT u FROM Usuario u WHERE u.rol = 'ANFITRION' AND u.estado = 'ACTIVA'")
    List<Usuario> findAnfitrionesActivos();

    @Query("SELECT u FROM Usuario u WHERE u.puntuacionPromedio >= :minPuntuacion ORDER BY u.puntuacionPromedio DESC")
    List<Usuario> findUsuariosConMejorReputacion(Double minPuntuacion);

    // Métodos para administración
    Long countByActivoTrue();
    
    Long countByBaneadoTrue();
    
    Long countByCreatedDateAfter(LocalDateTime fecha);
    
    Long countByRol(Rol rol);
    
    Page<Usuario> findByRol(Rol rol, Pageable pageable);
    
    Page<Usuario> findByBaneado(Boolean baneado, Pageable pageable);
    
    Page<Usuario> findByRolAndBaneado(Rol rol, Boolean baneado, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "u.telefono LIKE CONCAT('%', :query, '%')")
    Page<Usuario> buscarPorNombreEmailOTelefono(@Param("query") String query, Pageable pageable);
}
