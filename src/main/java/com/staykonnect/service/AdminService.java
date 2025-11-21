package com.staykonnect.service;

import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.entity.Valoracion;
import com.staykonnect.domain.entity.enums.EstadoPago;
import com.staykonnect.domain.entity.enums.EstadoReserva;
import com.staykonnect.domain.entity.enums.Rol;
import com.staykonnect.domain.repository.*;
import com.staykonnect.dto.admin.*;
import com.staykonnect.exception.BusinessException;
import com.staykonnect.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de administración del sistema
 * Gestiona usuarios, propiedades, reservas y métricas del dashboard
 */
@Slf4j
@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final PropiedadRepository propiedadRepository;
    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;
    private final ValoracionRepository valoracionRepository;
    private final MensajeRepository mensajeRepository;

    /**
     * Obtiene métricas generales del sistema para el dashboard
     */
    @Transactional(readOnly = true)
    public DashboardMetricasDTO obtenerMetricasDashboard() {
        log.info("Obteniendo métricas del dashboard");

        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        LocalDateTime inicioMesActual = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        // Métricas de usuarios
        Long totalUsuarios = usuarioRepository.count();
        Long usuariosActivos = usuarioRepository.countByActivoTrue();
        Long usuariosBaneados = usuarioRepository.countByBaneadoTrue();
        Long nuevosUsuarios = usuarioRepository.countByCreatedDateAfter(hace30Dias);
        Map<String, Long> usuariosPorRol = obtenerUsuariosPorRol();

        // Métricas de propiedades
        Long totalPropiedades = propiedadRepository.count();
        Long propiedadesAprobadas = propiedadRepository.countByAprobadaTrue();
        Long propiedadesPendientes = propiedadRepository.countByAprobadaFalseAndRechazadaFalse();
        Long propiedadesRechazadas = propiedadRepository.countByRechazadaTrue();
        Map<String, Long> propiedadesPorTipo = obtenerPropiedadesPorTipo();

        // Métricas de reservas
        Long totalReservas = reservaRepository.count();
        Long reservasPendientes = reservaRepository.countByEstado(EstadoReserva.PENDIENTE);
        Long reservasConfirmadas = reservaRepository.countByEstado(EstadoReserva.CONFIRMADA);
        Long reservasCompletadas = reservaRepository.countByEstado(EstadoReserva.COMPLETADA);
        Long reservasCanceladas = reservaRepository.countByEstado(EstadoReserva.CANCELADA);
        Map<String, Long> reservasPorEstado = obtenerReservasPorEstado();

        // Métricas de pagos
        BigDecimal ingresosTotales = pagoRepository.sumByEstado(EstadoPago.COMPLETADO);
        BigDecimal ingresosMesActual = pagoRepository.sumByEstadoAndFechaAfter(
            EstadoPago.COMPLETADO, 
            inicioMesActual
        );
        
        // Comisiones (10% de los pagos completados)
        BigDecimal comisionesTotales = ingresosTotales != null 
            ? ingresosTotales.multiply(new BigDecimal("0.10")) 
            : BigDecimal.ZERO;
        BigDecimal comisionesMesActual = ingresosMesActual != null 
            ? ingresosMesActual.multiply(new BigDecimal("0.10")) 
            : BigDecimal.ZERO;
        
        Long pagosPendientes = pagoRepository.countByEstado(EstadoPago.PENDIENTE);
        Long pagosCompletados = pagoRepository.countByEstado(EstadoPago.COMPLETADO);
        Long pagosFallidos = pagoRepository.countByEstado(EstadoPago.FALLIDO);

        // Métricas de valoraciones
        Long totalValoraciones = valoracionRepository.count();
        Double puntuacionPromedioGeneral = valoracionRepository.calcularPromedioGeneral();
        Long valoracionesPendientesModeracion = 0L; // TODO: Implementar lógica de moderación
        Long valoracionesOcultas = valoracionRepository.countByVisibleFalse();

        // Métricas de mensajería
        Long totalMensajes = mensajeRepository.count();
        Long mensajesUltimos30Dias = mensajeRepository.countByCreatedDateAfter(hace30Dias);
        Long conversacionesActivas = mensajeRepository.countConversacionesActivas();

        // Tasas de conversión
        Double tasaConversionReservas = totalReservas > 0 
            ? (reservasConfirmadas.doubleValue() / totalReservas.doubleValue()) * 100 
            : 0.0;
        Double tasaCompletamiento = reservasConfirmadas > 0 
            ? (reservasCompletadas.doubleValue() / reservasConfirmadas.doubleValue()) * 100 
            : 0.0;
        Double tasaCancelacion = totalReservas > 0 
            ? (reservasCanceladas.doubleValue() / totalReservas.doubleValue()) * 100 
            : 0.0;
        Double tasaAprobacionPropiedades = totalPropiedades > 0 
            ? (propiedadesAprobadas.doubleValue() / totalPropiedades.doubleValue()) * 100 
            : 0.0;

        return DashboardMetricasDTO.builder()
            .totalUsuarios(totalUsuarios)
            .usuariosActivos(usuariosActivos)
            .usuariosBaneados(usuariosBaneados)
            .nuevoUsuariosUltimos30Dias(nuevosUsuarios)
            .usuariosPorRol(usuariosPorRol)
            .totalPropiedades(totalPropiedades)
            .propiedadesAprobadas(propiedadesAprobadas)
            .propiedadesPendientes(propiedadesPendientes)
            .propiedadesRechazadas(propiedadesRechazadas)
            .propiedadesPorTipo(propiedadesPorTipo)
            .totalReservas(totalReservas)
            .reservasPendientes(reservasPendientes)
            .reservasConfirmadas(reservasConfirmadas)
            .reservasCompletadas(reservasCompletadas)
            .reservasCanceladas(reservasCanceladas)
            .reservasPorEstado(reservasPorEstado)
            .ingresosTotales(ingresosTotales != null ? ingresosTotales : BigDecimal.ZERO)
            .ingresosMesActual(ingresosMesActual != null ? ingresosMesActual : BigDecimal.ZERO)
            .comisionesTotales(comisionesTotales)
            .comisionesMesActual(comisionesMesActual)
            .pagosPendientes(pagosPendientes)
            .pagosCompletados(pagosCompletados)
            .pagosFallidos(pagosFallidos)
            .totalValoraciones(totalValoraciones)
            .puntuacionPromedioGeneral(puntuacionPromedioGeneral != null ? puntuacionPromedioGeneral : 0.0)
            .valoracionesPendientesModeracion(valoracionesPendientesModeracion)
            .valoracionesOcultas(valoracionesOcultas)
            .totalMensajes(totalMensajes)
            .mensajesUltimos30Dias(mensajesUltimos30Dias)
            .conversacionesActivas(conversacionesActivas)
            .tasaConversionReservas(tasaConversionReservas)
            .tasaCompletamiento(tasaCompletamiento)
            .tasaCancelacion(tasaCancelacion)
            .tasaAprobacionPropiedades(tasaAprobacionPropiedades)
            .build();
    }

    /**
     * Lista todos los usuarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<UsuarioAdminDTO> listarUsuarios(Pageable pageable) {
        log.info("Listando usuarios - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        return usuarioRepository.findAll(pageable)
            .map(this::convertirAUsuarioAdminDTO);
    }

    /**
     * Buscar usuarios por criterios
     */
    @Transactional(readOnly = true)
    public Page<UsuarioAdminDTO> buscarUsuarios(String query, Rol rol, Boolean baneado, Pageable pageable) {
        log.info("Buscando usuarios - Query: {}, Rol: {}, Baneado: {}", query, rol, baneado);
        
        Page<Usuario> usuarios;
        
        if (query != null && !query.trim().isEmpty()) {
            usuarios = usuarioRepository.buscarPorNombreEmailOTelefono(query, pageable);
        } else if (rol != null && baneado != null) {
            usuarios = usuarioRepository.findByRolAndBaneado(rol, baneado, pageable);
        } else if (rol != null) {
            usuarios = usuarioRepository.findByRol(rol, pageable);
        } else if (baneado != null) {
            usuarios = usuarioRepository.findByBaneado(baneado, pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }
        
        return usuarios.map(this::convertirAUsuarioAdminDTO);
    }

    /**
     * Banear un usuario
     */
    @Transactional
    public UsuarioAdminDTO banearUsuario(Long usuarioId, BanearUsuarioRequest request) {
        log.info("Baneando usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        if (usuario.getBaneado()) {
            throw new BusinessException("El usuario ya está baneado");
        }
        
        if (usuario.getRol() == Rol.ADMIN) {
            throw new BusinessException("No se puede banear a un administrador");
        }
        
        Usuario admin = obtenerUsuarioActual();
        
        usuario.setBaneado(true);
        usuario.setFechaBaneo(LocalDateTime.now());
        usuario.setRazonBaneo(request.getRazon());
        usuario.setBaneadoPor(admin);
        usuario.setActivo(false); // Desactivar también la cuenta
        
        usuario = usuarioRepository.save(usuario);
        log.info("Usuario baneado exitosamente: {}", usuario.getEmail());
        
        return convertirAUsuarioAdminDTO(usuario);
    }

    /**
     * Desbanear un usuario
     */
    @Transactional
    public UsuarioAdminDTO desbanearUsuario(Long usuarioId) {
        log.info("Desbaneando usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        if (!usuario.getBaneado()) {
            throw new BusinessException("El usuario no está baneado");
        }
        
        usuario.setBaneado(false);
        usuario.setFechaBaneo(null);
        usuario.setRazonBaneo(null);
        usuario.setBaneadoPor(null);
        usuario.setActivo(true); // Reactivar la cuenta
        
        usuario = usuarioRepository.save(usuario);
        log.info("Usuario desbaneado exitosamente: {}", usuario.getEmail());
        
        return convertirAUsuarioAdminDTO(usuario);
    }

    /**
     * Eliminar un usuario (soft delete si es posible)
     */
    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        log.info("Eliminando usuario ID: {}", usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        if (usuario.getRol() == Rol.ADMIN) {
            throw new BusinessException("No se puede eliminar a un administrador");
        }
        
        // Verificar si tiene reservas activas
        Long reservasActivas = reservaRepository.countByViajeroAndEstadoIn(
            usuario,
            java.util.Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA, EstadoReserva.PAGADA)
        );
        
        if (reservasActivas > 0) {
            throw new BusinessException("No se puede eliminar un usuario con reservas activas. " +
                "Tiene " + reservasActivas + " reserva(s) pendiente(s). Considere banearlo en su lugar.");
        }
        
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado exitosamente: {}", usuario.getEmail());
    }

    /**
     * Lista todas las propiedades con paginación
     */
    @Transactional(readOnly = true)
    public Page<PropiedadAdminDTO> listarPropiedades(Pageable pageable) {
        log.info("Listando propiedades - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        return propiedadRepository.findAll(pageable)
            .map(this::convertirAPropiedadAdminDTO);
    }

    /**
     * Lista propiedades pendientes de aprobación
     */
    @Transactional(readOnly = true)
    public Page<PropiedadAdminDTO> listarPropiedadesPendientes(Pageable pageable) {
        log.info("Listando propiedades pendientes de aprobación");
        
        return propiedadRepository.findByAprobadaFalseAndRechazadaFalse(pageable)
            .map(this::convertirAPropiedadAdminDTO);
    }

    /**
     * Aprobar una propiedad
     */
    @Transactional
    public PropiedadAdminDTO aprobarPropiedad(Long propiedadId) {
        log.info("Aprobando propiedad ID: {}", propiedadId);
        
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
            .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        
        if (propiedad.getAprobada()) {
            throw new BusinessException("La propiedad ya está aprobada");
        }
        
        Usuario admin = obtenerUsuarioActual();
        
        propiedad.setAprobada(true);
        propiedad.setFechaAprobacion(LocalDateTime.now());
        propiedad.setAprobadaPor(admin);
        propiedad.setRechazada(false);
        propiedad.setFechaRechazo(null);
        propiedad.setRazonRechazo(null);
        propiedad.setRechazadaPor(null);
        
        propiedad = propiedadRepository.save(propiedad);
        log.info("Propiedad aprobada exitosamente: {}", propiedad.getTitulo());
        
        // TODO: Enviar notificación al anfitrión
        
        return convertirAPropiedadAdminDTO(propiedad);
    }

    /**
     * Rechazar una propiedad
     */
    @Transactional
    public PropiedadAdminDTO rechazarPropiedad(Long propiedadId, RechazarPropiedadRequest request) {
        log.info("Rechazando propiedad ID: {}", propiedadId);
        
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
            .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        
        if (propiedad.getRechazada()) {
            throw new BusinessException("La propiedad ya está rechazada");
        }
        
        Usuario admin = obtenerUsuarioActual();
        
        propiedad.setRechazada(true);
        propiedad.setFechaRechazo(LocalDateTime.now());
        propiedad.setRazonRechazo(request.getRazon());
        propiedad.setRechazadaPor(admin);
        propiedad.setAprobada(false);
        propiedad.setFechaAprobacion(null);
        propiedad.setAprobadaPor(null);
        
        propiedad = propiedadRepository.save(propiedad);
        log.info("Propiedad rechazada exitosamente: {}", propiedad.getTitulo());
        
        // TODO: Enviar notificación al anfitrión con la razón del rechazo
        
        return convertirAPropiedadAdminDTO(propiedad);
    }

    /**
     * Ocultar/mostrar una propiedad
     */
    @Transactional
    public PropiedadAdminDTO toggleVisibilidadPropiedad(Long propiedadId) {
        log.info("Cambiando visibilidad de propiedad ID: {}", propiedadId);
        
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
            .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
        
        // TODO: Implementar campo 'visible' en Propiedad si no existe
        // propiedad.setVisible(!propiedad.getVisible());
        
        propiedad = propiedadRepository.save(propiedad);
        log.info("Visibilidad de propiedad cambiada: {}", propiedad.getTitulo());
        
        return convertirAPropiedadAdminDTO(propiedad);
    }

    /**
     * Lista todas las reservas con paginación
     */
    @Transactional(readOnly = true)
    public Page<ReservaAdminDTO> listarReservas(Pageable pageable) {
        log.info("Listando reservas - Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        return reservaRepository.findAll(pageable)
            .map(this::convertirAReservaAdminDTO);
    }

    /**
     * Buscar reservas por estado
     */
    @Transactional(readOnly = true)
    public Page<ReservaAdminDTO> buscarReservasPorEstado(EstadoReserva estado, Pageable pageable) {
        log.info("Buscando reservas por estado: {}", estado);
        
        return reservaRepository.findByEstado(estado, pageable)
            .map(this::convertirAReservaAdminDTO);
    }

    /**
     * Cancelar una reserva (admin override)
     */
    @Transactional
    public ReservaAdminDTO cancelarReserva(Long reservaId, String razon) {
        log.info("Admin cancelando reserva ID: {}", reservaId);
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
        
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new BusinessException("La reserva ya está cancelada");
        }
        
        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new BusinessException("No se puede cancelar una reserva completada");
        }
        
        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva = reservaRepository.save(reserva);
        
        log.info("Reserva cancelada por admin - ID: {}, Razón: {}", reservaId, razon);
        
        // TODO: Procesar reembolso si corresponde
        // TODO: Enviar notificaciones a viajero y anfitrión
        
        return convertirAReservaAdminDTO(reserva);
    }

    /**
     * Ocultar una valoración
     */
    @Transactional
    public void ocultarValoracion(Long valoracionId) {
        log.info("Ocultando valoración ID: {}", valoracionId);
        
        Valoracion valoracion = valoracionRepository.findById(valoracionId)
            .orElseThrow(() -> new ResourceNotFoundException("Valoración no encontrada"));
        
        valoracion.setVisible(false);
        valoracionRepository.save(valoracion);
        
        log.info("Valoración ocultada exitosamente");
    }

    /**
     * Mostrar una valoración previamente oculta
     */
    @Transactional
    public void mostrarValoracion(Long valoracionId) {
        log.info("Mostrando valoración ID: {}", valoracionId);
        
        Valoracion valoracion = valoracionRepository.findById(valoracionId)
            .orElseThrow(() -> new ResourceNotFoundException("Valoración no encontrada"));
        
        valoracion.setVisible(true);
        valoracionRepository.save(valoracion);
        
        log.info("Valoración mostrada exitosamente");
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private Map<String, Long> obtenerUsuariosPorRol() {
        Map<String, Long> map = new HashMap<>();
        for (Rol rol : Rol.values()) {
            Long count = usuarioRepository.countByRol(rol);
            map.put(rol.name(), count);
        }
        return map;
    }

    private Map<String, Long> obtenerPropiedadesPorTipo() {
        Map<String, Long> map = new HashMap<>();
        // TODO: Implementar query en PropiedadRepository
        return map;
    }

    private Map<String, Long> obtenerReservasPorEstado() {
        Map<String, Long> map = new HashMap<>();
        for (EstadoReserva estado : EstadoReserva.values()) {
            Long count = reservaRepository.countByEstado(estado);
            map.put(estado.name(), count);
        }
        return map;
    }

    private UsuarioAdminDTO convertirAUsuarioAdminDTO(Usuario usuario) {
        Long totalPropiedades = propiedadRepository.countByAnfitrionId(usuario.getId());
        Long reservasViajero = reservaRepository.countByViajeroId(usuario.getId());
        Long reservasAnfitrion = reservaRepository.countByPropiedadAnfitrionId(usuario.getId());
        Long valoraciones = valoracionRepository.countByValoradoId(usuario.getId());
        Double puntuacion = valoracionRepository.calcularPromedioAnfitrion(usuario.getId());
        Long mensajes = mensajeRepository.countByRemitenteId(usuario.getId());
        
        return UsuarioAdminDTO.builder()
            .id(usuario.getId())
            .nombre(usuario.getNombre())
            .apellido(usuario.getApellido())
            .email(usuario.getEmail())
            .telefono(usuario.getTelefono())
            .rol(usuario.getRol())
            .emailVerificado(usuario.getEmailVerificado())
            .activo(usuario.getActivo())
            .baneado(usuario.getBaneado())
            .fechaBaneo(usuario.getFechaBaneo())
            .razonBaneo(usuario.getRazonBaneo())
            .baneadoPorNombre(usuario.getBaneadoPor() != null ? usuario.getBaneadoPor().getNombreCompleto() : null)
            .createdDate(usuario.getCreatedDate())
            .lastModifiedDate(usuario.getLastModifiedDate())
            .totalPropiedades(totalPropiedades)
            .totalReservasComoViajero(reservasViajero)
            .totalReservasComoAnfitrion(reservasAnfitrion)
            .totalValoracionesRecibidas(valoraciones)
            .puntuacionPromedio(puntuacion)
            .totalMensajesEnviados(mensajes)
            .build();
    }

    private PropiedadAdminDTO convertirAPropiedadAdminDTO(Propiedad propiedad) {
        Long totalReservas = reservaRepository.countByPropiedadId(propiedad.getId());
        Long reservasCompletadas = reservaRepository.countByPropiedadIdAndEstado(
            propiedad.getId(), 
            EstadoReserva.COMPLETADA
        );
        Long valoraciones = valoracionRepository.countByPropiedadId(propiedad.getId());
        BigDecimal ingresos = reservaRepository.sumPrecioTotalByPropiedadIdAndEstado(
            propiedad.getId(), 
            EstadoReserva.COMPLETADA
        );
        
        return PropiedadAdminDTO.builder()
            .id(propiedad.getId())
            .titulo(propiedad.getTitulo())
            .descripcion(propiedad.getDescripcion())
            .tipo(propiedad.getTipo())
            .estado(propiedad.getEstado())
            .precioPorNoche(propiedad.getPrecioPorNoche())
            .ciudad(propiedad.getCiudad())
            .pais(propiedad.getPais())
            .capacidadMaxima(propiedad.getCapidadMaxima())
            .aprobada(propiedad.getAprobada())
            .fechaAprobacion(propiedad.getFechaAprobacion())
            .aprobadaPorNombre(propiedad.getAprobadaPor() != null ? propiedad.getAprobadaPor().getNombreCompleto() : null)
            .rechazada(propiedad.getRechazada())
            .fechaRechazo(propiedad.getFechaRechazo())
            .razonRechazo(propiedad.getRazonRechazo())
            .rechazadaPorNombre(propiedad.getRechazadaPor() != null ? propiedad.getRechazadaPor().getNombreCompleto() : null)
            .createdDate(propiedad.getCreatedDate())
            .lastModifiedDate(propiedad.getLastModifiedDate())
            .anfitrionId(propiedad.getAnfitrion().getId())
            .anfitrionNombre(propiedad.getAnfitrion().getNombreCompleto())
            .anfitrionEmail(propiedad.getAnfitrion().getEmail())
            .totalReservas(totalReservas)
            .reservasCompletadas(reservasCompletadas)
            .totalValoraciones(valoraciones)
            .puntuacionPromedio(propiedad.getPuntuacionPromedio())
            .ingresosGenerados(ingresos != null ? ingresos : BigDecimal.ZERO)
            .build();
    }

    private ReservaAdminDTO convertirAReservaAdminDTO(Reserva reserva) {
        Valoracion valoracion = valoracionRepository.findByReservaId(reserva.getId()).orElse(null);
        
        return ReservaAdminDTO.builder()
            .id(reserva.getId())
            .estado(reserva.getEstado())
            .fechaCheckin(reserva.getFechaCheckin())
            .fechaCheckout(reserva.getFechaCheckout())
            .numeroHuespedes(reserva.getNumeroHuespedes())
            .precioTotalNoche(reserva.getPrecioTotalNoche())
            .costoLimpieza(reserva.getCostoLimpieza())
            .comisionPlataforma(reserva.getComisionPlataforma())
            .precioTotal(reserva.getPrecioTotal())
            .codigoPago(reserva.getCodigoPago())
            .createdDate(reserva.getCreatedDate())
            .lastModifiedDate(reserva.getLastModifiedDate())
            .viajeroId(reserva.getViajero().getId())
            .viajeroNombre(reserva.getViajero().getNombreCompleto())
            .viajeroEmail(reserva.getViajero().getEmail())
            .viajeroTelefono(reserva.getViajero().getTelefono())
            .anfitrionId(reserva.getPropiedad().getAnfitrion().getId())
            .anfitrionNombre(reserva.getPropiedad().getAnfitrion().getNombreCompleto())
            .anfitrionEmail(reserva.getPropiedad().getAnfitrion().getEmail())
            .propiedadId(reserva.getPropiedad().getId())
            .propiedadTitulo(reserva.getPropiedad().getTitulo())
            .propiedadCiudad(reserva.getPropiedad().getCiudad())
            .propiedadPais(reserva.getPropiedad().getPais())
            .pagoEstado(reserva.getPago() != null ? reserva.getPago().getEstado().name() : null)
            .pagoMetodo(reserva.getPago() != null ? reserva.getPago().getMetodo().name() : null)
            .tieneValoracion(valoracion != null)
            .valoracionPuntuacion(valoracion != null ? valoracion.getPuntuacion() : null)
            .build();
    }
}
