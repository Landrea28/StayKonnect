package com.staykonnect.service;

import com.staykonnect.common.exception.BusinessException;
import com.staykonnect.common.exception.ResourceNotFoundException;
import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.RolUsuario;
import com.staykonnect.domain.repository.PropiedadRepository;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.propiedad.*;
import com.staykonnect.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de propiedades.
 * Maneja el CRUD completo y validaciones de negocio.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    /**
     * Crea una nueva propiedad.
     * Solo anfitriones pueden crear propiedades.
     *
     * @param request Datos de la propiedad
     * @return Propiedad creada
     */
    @Transactional
    public PropiedadDTO crearPropiedad(CrearPropiedadRequest request) {
        Usuario anfitrion = getUsuarioAutenticado();
        
        log.info("Creando nueva propiedad para anfitrión: {}", anfitrion.getEmail());

        // Validar que el usuario sea anfitrión
        if (!anfitrion.esAnfitrion()) {
            throw new BusinessException("Solo los anfitriones pueden crear propiedades");
        }

        // Validar coherencia de estancias
        if (request.getEstanciaMaxima() != null && request.getEstanciaMinima() != null) {
            if (request.getEstanciaMaxima() < request.getEstanciaMinima()) {
                throw new BusinessException("La estancia máxima no puede ser menor que la estancia mínima");
            }
        }

        // Crear entidad
        Propiedad propiedad = Propiedad.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipoPropiedad(request.getTipoPropiedad())
                .direccion(request.getDireccion())
                .ciudad(request.getCiudad())
                .pais(request.getPais())
                .codigoPostal(request.getCodigoPostal())
                .latitud(request.getLatitud())
                .longitud(request.getLongitud())
                .habitaciones(request.getHabitaciones())
                .camas(request.getCamas())
                .banos(request.getBanos())
                .capacidad(request.getCapacidad())
                .areaM2(request.getAreaM2())
                .precioPorNoche(request.getPrecioPorNoche())
                .precioLimpieza(request.getPrecioLimpieza() != null ? request.getPrecioLimpieza() : java.math.BigDecimal.ZERO)
                .depositoSeguridad(request.getDepositoSeguridad() != null ? request.getDepositoSeguridad() : java.math.BigDecimal.ZERO)
                .servicios(request.getServicios())
                .imagenes(request.getImagenes())
                .reglasCasa(request.getReglasCasa())
                .horaCheckin(request.getHoraCheckin())
                .horaCheckout(request.getHoraCheckout())
                .estanciaMinima(request.getEstanciaMinima() != null ? request.getEstanciaMinima() : 1)
                .estanciaMaxima(request.getEstanciaMaxima() != null ? request.getEstanciaMaxima() : 365)
                .estado(EstadoPropiedad.PENDIENTE_APROBACION)
                .anfitrion(anfitrion)
                .puntuacionPromedio(0.0)
                .totalValoraciones(0)
                .totalReservas(0)
                .verificacionCompleta(false)
                .build();

        propiedad = propiedadRepository.save(propiedad);
        log.info("Propiedad creada con ID: {}", propiedad.getId());

        return convertirADTO(propiedad);
    }

    /**
     * Obtiene una propiedad por ID.
     *
     * @param id ID de la propiedad
     * @return Propiedad encontrada
     */
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "propiedades", key = "#id")
    public PropiedadDTO obtenerPropiedad(Long id) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        return convertirADTO(propiedad);
    }

    /**
     * Lista todas las propiedades del anfitrión autenticado.
     *
     * @param pageable Configuración de paginación
     * @return Página de propiedades
     */
    @Transactional(readOnly = true)
    public Page<PropiedadResumenDTO> listarMisPropiedades(Pageable pageable) {
        Usuario anfitrion = getUsuarioAutenticado();
        
        log.info("Listando propiedades del anfitrión: {}", anfitrion.getEmail());

        Page<Propiedad> propiedades = propiedadRepository.findByAnfitrionId(anfitrion.getId(), pageable);
        
        return propiedades.map(this::convertirAResumenDTO);
    }

    /**
     * Lista propiedades activas (público).
     *
     * @param pageable Configuración de paginación
     * @return Página de propiedades activas
     */
    @Transactional(readOnly = true)
    public Page<PropiedadResumenDTO> listarPropiedadesActivas(Pageable pageable) {
        Page<Propiedad> propiedades = propiedadRepository.findByEstado(EstadoPropiedad.ACTIVA, pageable);
        return propiedades.map(this::convertirAResumenDTO);
    }

    /**
     * Actualiza una propiedad existente.
     * Solo el anfitrión propietario puede actualizar.
     *
     * @param id ID de la propiedad
     * @param request Datos a actualizar
     * @return Propiedad actualizada
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "propiedades", key = "#id")
    public PropiedadDTO actualizarPropiedad(Long id, ActualizarPropiedadRequest request) {
        Usuario usuario = getUsuarioAutenticado();
        
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Verificar que sea el propietario o admin
        if (!esPropioPropietarioOAdmin(propiedad, usuario)) {
            throw new AccessDeniedException("No tienes permiso para actualizar esta propiedad");
        }

        log.info("Actualizando propiedad ID: {} por usuario: {}", id, usuario.getEmail());

        // Actualizar solo campos no nulos
        if (request.getTitulo() != null) propiedad.setTitulo(request.getTitulo());
        if (request.getDescripcion() != null) propiedad.setDescripcion(request.getDescripcion());
        if (request.getTipoPropiedad() != null) propiedad.setTipoPropiedad(request.getTipoPropiedad());
        if (request.getDireccion() != null) propiedad.setDireccion(request.getDireccion());
        if (request.getCiudad() != null) propiedad.setCiudad(request.getCiudad());
        if (request.getPais() != null) propiedad.setPais(request.getPais());
        if (request.getCodigoPostal() != null) propiedad.setCodigoPostal(request.getCodigoPostal());
        if (request.getLatitud() != null) propiedad.setLatitud(request.getLatitud());
        if (request.getLongitud() != null) propiedad.setLongitud(request.getLongitud());
        if (request.getHabitaciones() != null) propiedad.setHabitaciones(request.getHabitaciones());
        if (request.getCamas() != null) propiedad.setCamas(request.getCamas());
        if (request.getBanos() != null) propiedad.setBanos(request.getBanos());
        if (request.getCapacidad() != null) propiedad.setCapacidad(request.getCapacidad());
        if (request.getAreaM2() != null) propiedad.setAreaM2(request.getAreaM2());
        if (request.getPrecioPorNoche() != null) propiedad.setPrecioPorNoche(request.getPrecioPorNoche());
        if (request.getPrecioLimpieza() != null) propiedad.setPrecioLimpieza(request.getPrecioLimpieza());
        if (request.getDepositoSeguridad() != null) propiedad.setDepositoSeguridad(request.getDepositoSeguridad());
        if (request.getServicios() != null) propiedad.setServicios(request.getServicios());
        if (request.getImagenes() != null) propiedad.setImagenes(request.getImagenes());
        if (request.getReglasCasa() != null) propiedad.setReglasCasa(request.getReglasCasa());
        if (request.getHoraCheckin() != null) propiedad.setHoraCheckin(request.getHoraCheckin());
        if (request.getHoraCheckout() != null) propiedad.setHoraCheckout(request.getHoraCheckout());
        if (request.getEstanciaMinima() != null) propiedad.setEstanciaMinima(request.getEstanciaMinima());
        if (request.getEstanciaMaxima() != null) propiedad.setEstanciaMaxima(request.getEstanciaMaxima());

        // Validar coherencia de estancias si se actualizaron ambas
        if (propiedad.getEstanciaMaxima() < propiedad.getEstanciaMinima()) {
            throw new BusinessException("La estancia máxima no puede ser menor que la estancia mínima");
        }

        propiedad = propiedadRepository.save(propiedad);
        log.info("Propiedad actualizada con ID: {}", propiedad.getId());

        return convertirADTO(propiedad);
    }

    /**
     * Cambia el estado de una propiedad.
     *
     * @param id ID de la propiedad
     * @param nuevoEstado Nuevo estado
     * @return Propiedad actualizada
     */
    @Transactional
    public PropiedadDTO cambiarEstadoPropiedad(Long id, EstadoPropiedad nuevoEstado) {
        Usuario usuario = getUsuarioAutenticado();
        
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Verificar permisos
        if (!esPropioPropietarioOAdmin(propiedad, usuario)) {
            throw new AccessDeniedException("No tienes permiso para cambiar el estado de esta propiedad");
        }

        log.info("Cambiando estado de propiedad ID: {} de {} a {}", id, propiedad.getEstado(), nuevoEstado);

        propiedad.setEstado(nuevoEstado);
        propiedad = propiedadRepository.save(propiedad);

        return convertirADTO(propiedad);
    }

    /**
     * Elimina (lógicamente) una propiedad.
     * Cambia su estado a ELIMINADA en lugar de borrarla físicamente.
     *
     * @param id ID de la propiedad
     */
    @Transactional
    public void eliminarPropiedad(Long id) {
        Usuario usuario = getUsuarioAutenticado();
        
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada con id: " + id));

        // Verificar permisos
        if (!esPropioPropietarioOAdmin(propiedad, usuario)) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta propiedad");
        }

        // Verificar que no tenga reservas activas
        boolean tieneReservasActivas = propiedad.getReservas() != null && 
                propiedad.getReservas().stream()
                        .anyMatch(r -> r.getEstado().toString().matches("PENDIENTE|CONFIRMADA|PAGADA|EN_CURSO"));

        if (tieneReservasActivas) {
            throw new BusinessException("No se puede eliminar una propiedad con reservas activas");
        }

        log.info("Eliminando propiedad ID: {} por usuario: {}", id, usuario.getEmail());

        propiedad.setEstado(EstadoPropiedad.ELIMINADA);
        propiedadRepository.save(propiedad);
    }

    /**
     * Obtiene el usuario autenticado del contexto de seguridad.
     *
     * @return Usuario autenticado
     */
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        return usuarioRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Verifica si el usuario es el propietario de la propiedad o un administrador.
     *
     * @param propiedad Propiedad a verificar
     * @param usuario Usuario a verificar
     * @return true si es propietario o admin
     */
    private boolean esPropioPropietarioOAdmin(Propiedad propiedad, Usuario usuario) {
        return propiedad.getAnfitrion().getId().equals(usuario.getId()) || 
               usuario.getRol() == RolUsuario.ADMIN;
    }

    /**
     * Convierte una entidad Propiedad a PropiedadDTO.
     *
     * @param propiedad Entidad a convertir
     * @return DTO de propiedad
     */
    private PropiedadDTO convertirADTO(Propiedad propiedad) {
        PropiedadDTO dto = modelMapper.map(propiedad, PropiedadDTO.class);
        dto.setAnfitrionId(propiedad.getAnfitrion().getId());
        dto.setAnfitrionNombre(propiedad.getAnfitrion().getNombreCompleto());
        dto.setAnfitrionEmail(propiedad.getAnfitrion().getEmail());
        return dto;
    }

    /**
     * Convierte una entidad Propiedad a PropiedadResumenDTO.
     *
     * @param propiedad Entidad a convertir
     * @return DTO resumido de propiedad
     */
    private PropiedadResumenDTO convertirAResumenDTO(Propiedad propiedad) {
        PropiedadResumenDTO dto = modelMapper.map(propiedad, PropiedadResumenDTO.class);
        dto.setAnfitrionNombre(propiedad.getAnfitrion().getNombreCompleto());
        dto.setImagenPrincipal(propiedad.getImagenes() != null && !propiedad.getImagenes().isEmpty() 
                ? propiedad.getImagenes().iterator().next()
                : null);
        return dto;
    }
}
