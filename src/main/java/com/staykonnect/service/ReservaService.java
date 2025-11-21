package com.staykonnect.service;

import com.staykonnect.common.exception.BadRequestException;
import com.staykonnect.common.exception.ForbiddenException;
import com.staykonnect.common.exception.ResourceNotFoundException;
import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.EstadoPropiedad;
import com.staykonnect.domain.enums.EstadoReserva;
import com.staykonnect.domain.enums.Rol;
import com.staykonnect.domain.repository.PropiedadRepository;
import com.staykonnect.domain.repository.ReservaRepository;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.reserva.CancelarReservaRequest;
import com.staykonnect.dto.reserva.CrearReservaRequest;
import com.staykonnect.dto.reserva.ReservaDTO;
import com.staykonnect.dto.reserva.ReservaResumenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio para gestión de reservas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    private static final BigDecimal COMISION_PLATAFORMA_PORCENTAJE = new BigDecimal("0.10"); // 10%

    /**
     * Crea una nueva solicitud de reserva.
     */
    @Transactional
    public ReservaDTO crearReserva(CrearReservaRequest request) {
        log.info("Creando nueva reserva para propiedad ID: {}", request.getPropiedadId());

        Usuario viajero = getUsuarioAutenticado();
        
        // Validar que el usuario sea viajero
        if (!viajero.getRoles().contains(Rol.VIAJERO)) {
            throw new ForbiddenException("Solo los usuarios con rol VIAJERO pueden hacer reservas");
        }

        // Obtener propiedad
        Propiedad propiedad = propiedadRepository.findById(request.getPropiedadId())
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        // Validar que la propiedad esté activa
        if (propiedad.getEstado() != EstadoPropiedad.ACTIVA) {
            throw new BadRequestException("La propiedad no está disponible para reservas");
        }

        // Validar que el viajero no sea el anfitrión de la propiedad
        if (propiedad.getAnfitrion().getId().equals(viajero.getId())) {
            throw new BadRequestException("No puedes reservar tu propia propiedad");
        }

        // Validar fechas
        validarFechas(request.getFechaCheckin(), request.getFechaCheckout(), propiedad);

        // Validar disponibilidad
        validarDisponibilidad(request.getPropiedadId(), request.getFechaCheckin(), request.getFechaCheckout());

        // Validar capacidad
        if (request.getNumeroHuespedes() > propiedad.getCapacidad()) {
            throw new BadRequestException(
                    String.format("La propiedad tiene capacidad máxima de %d huéspedes", propiedad.getCapacidad())
            );
        }

        // Calcular costos
        int numeroNoches = (int) ChronoUnit.DAYS.between(request.getFechaCheckin(), request.getFechaCheckout());
        BigDecimal subtotal = propiedad.getPrecioPorNoche().multiply(BigDecimal.valueOf(numeroNoches));
        BigDecimal precioLimpieza = propiedad.getPrecioLimpieza() != null ? propiedad.getPrecioLimpieza() : BigDecimal.ZERO;
        BigDecimal depositoSeguridad = propiedad.getDepositoSeguridad() != null ? propiedad.getDepositoSeguridad() : BigDecimal.ZERO;
        BigDecimal comision = subtotal.multiply(COMISION_PLATAFORMA_PORCENTAJE);
        BigDecimal precioTotal = subtotal.add(precioLimpieza).add(comision);

        // Crear reserva
        Reserva reserva = Reserva.builder()
                .propiedad(propiedad)
                .viajero(viajero)
                .estado(EstadoReserva.PENDIENTE)
                .fechaCheckin(request.getFechaCheckin())
                .fechaCheckout(request.getFechaCheckout())
                .numeroHuespedes(request.getNumeroHuespedes())
                .notasEspeciales(request.getNotasEspeciales())
                .precioTotal(precioTotal)
                .precioNoche(propiedad.getPrecioPorNoche())
                .precioLimpieza(precioLimpieza)
                .depositoSeguridad(depositoSeguridad)
                .comisionPlataforma(comision)
                .build();

        reserva = reservaRepository.save(reserva);
        log.info("Reserva creada exitosamente con ID: {}", reserva.getId());

        return convertirADTO(reserva);
    }

    /**
     * Confirma una reserva (solo anfitrión).
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "reservas", key = "#reservaId")
    public ReservaDTO confirmarReserva(Long reservaId) {
        log.info("Confirmando reserva ID: {}", reservaId);

        Reserva reserva = obtenerReservaPorId(reservaId);
        Usuario usuario = getUsuarioAutenticado();

        // Validar que el usuario sea el anfitrión de la propiedad
        if (!reserva.getPropiedad().getAnfitrion().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Solo el anfitrión de la propiedad puede confirmar reservas");
        }

        // Validar estado
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BadRequestException("Solo se pueden confirmar reservas en estado PENDIENTE");
        }

        // Validar disponibilidad nuevamente por seguridad
        validarDisponibilidad(
                reserva.getPropiedad().getId(),
                reserva.getFechaCheckin(),
                reserva.getFechaCheckout(),
                reservaId
        );

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setFechaConfirmacion(LocalDateTime.now());
        reserva = reservaRepository.save(reserva);

        log.info("Reserva {} confirmada exitosamente", reservaId);
        return convertirADTO(reserva);
    }

    /**
     * Cancela una reserva.
     */
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "reservas", key = "#reservaId")
    public ReservaDTO cancelarReserva(Long reservaId, CancelarReservaRequest request) {
        log.info("Cancelando reserva ID: {}", reservaId);

        Reserva reserva = obtenerReservaPorId(reservaId);
        Usuario usuario = getUsuarioAutenticado();

        // Validar permisos (viajero o anfitrión)
        boolean esViajero = reserva.getViajero().getId().equals(usuario.getId());
        boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(usuario.getId());

        if (!esViajero && !esAnfitrion) {
            throw new ForbiddenException("No tienes permiso para cancelar esta reserva");
        }

        // Validar que la reserva se pueda cancelar
        if (reserva.getEstado() != EstadoReserva.PENDIENTE && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new BadRequestException("La reserva no se puede cancelar en su estado actual");
        }

        // No permitir cancelación si el check-in es en menos de 24 horas
        if (ChronoUnit.HOURS.between(LocalDateTime.now(), reserva.getFechaCheckin().atStartOfDay()) < 24) {
            throw new BadRequestException("No se puede cancelar una reserva con menos de 24 horas antes del check-in");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setNotasEspeciales(
                (reserva.getNotasEspeciales() != null ? reserva.getNotasEspeciales() + "\n\n" : "") +
                "CANCELACIÓN: " + request.getMotivoCancelacion()
        );

        reserva = reservaRepository.save(reserva);
        log.info("Reserva {} cancelada exitosamente", reservaId);

        return convertirADTO(reserva);
    }

    /**
     * Rechaza una reserva (solo anfitrión).
     */
    @Transactional
    public ReservaDTO rechazarReserva(Long reservaId, String motivo) {
        log.info("Rechazando reserva ID: {}", reservaId);

        Reserva reserva = obtenerReservaPorId(reservaId);
        Usuario usuario = getUsuarioAutenticado();

        // Validar que el usuario sea el anfitrión
        if (!reserva.getPropiedad().getAnfitrion().getId().equals(usuario.getId())) {
            throw new ForbiddenException("Solo el anfitrión puede rechazar reservas");
        }

        // Validar estado
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new BadRequestException("Solo se pueden rechazar reservas en estado PENDIENTE");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setNotasEspeciales(
                (reserva.getNotasEspeciales() != null ? reserva.getNotasEspeciales() + "\n\n" : "") +
                "RECHAZADA POR ANFITRIÓN: " + motivo
        );

        reserva = reservaRepository.save(reserva);
        log.info("Reserva {} rechazada exitosamente", reservaId);

        return convertirADTO(reserva);
    }

    /**
     * Obtiene una reserva por ID.
     */
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "reservas", key = "#id")
    public ReservaDTO obtenerReserva(Long id) {
        Reserva reserva = obtenerReservaPorId(id);
        Usuario usuario = getUsuarioAutenticado();

        // Validar permisos
        boolean esViajero = reserva.getViajero().getId().equals(usuario.getId());
        boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRoles().contains(Rol.ADMIN);

        if (!esViajero && !esAnfitrion && !esAdmin) {
            throw new ForbiddenException("No tienes permiso para ver esta reserva");
        }

        return convertirADTO(reserva);
    }

    /**
     * Lista las reservas del usuario autenticado como viajero.
     */
    @Transactional(readOnly = true)
    public Page<ReservaResumenDTO> listarMisReservasComoViajero(Pageable pageable) {
        Usuario viajero = getUsuarioAutenticado();
        Page<Reserva> reservas = reservaRepository.findByViajeroIdOrderByCreatedDateDesc(viajero.getId(), pageable);
        return reservas.map(this::convertirAResumenDTO);
    }

    /**
     * Lista las reservas de las propiedades del anfitrión.
     */
    @Transactional(readOnly = true)
    public Page<ReservaResumenDTO> listarReservasComoAnfitrion(Pageable pageable) {
        Usuario anfitrion = getUsuarioAutenticado();
        Page<Reserva> reservas = reservaRepository.findByPropiedadAnfitrionIdOrderByCreatedDateDesc(anfitrion.getId(), pageable);
        return reservas.map(this::convertirAResumenDTO);
    }

    /**
     * Lista las reservas de una propiedad específica (solo anfitrión o admin).
     */
    @Transactional(readOnly = true)
    public Page<ReservaResumenDTO> listarReservasPorPropiedad(Long propiedadId, Pageable pageable) {
        Usuario usuario = getUsuarioAutenticado();
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));

        // Validar permisos
        boolean esAnfitrion = propiedad.getAnfitrion().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRoles().contains(Rol.ADMIN);

        if (!esAnfitrion && !esAdmin) {
            throw new ForbiddenException("No tienes permiso para ver las reservas de esta propiedad");
        }

        Page<Reserva> reservas = reservaRepository.findByPropiedadIdOrderByFechaCheckinDesc(propiedadId, pageable);
        return reservas.map(this::convertirAResumenDTO);
    }

    /**
     * Valida que las fechas sean correctas.
     */
    private void validarFechas(LocalDate checkin, LocalDate checkout, Propiedad propiedad) {
        // Validar que checkout sea después de checkin
        if (!checkout.isAfter(checkin)) {
            throw new BadRequestException("La fecha de check-out debe ser posterior a la de check-in");
        }

        // Validar estancia mínima
        long numeroNoches = ChronoUnit.DAYS.between(checkin, checkout);
        if (propiedad.getEstanciaMinima() != null && numeroNoches < propiedad.getEstanciaMinima()) {
            throw new BadRequestException(
                    String.format("La estancia mínima es de %d noches", propiedad.getEstanciaMinima())
            );
        }

        // Validar estancia máxima
        if (propiedad.getEstanciaMaxima() != null && numeroNoches > propiedad.getEstanciaMaxima()) {
            throw new BadRequestException(
                    String.format("La estancia máxima es de %d noches", propiedad.getEstanciaMaxima())
            );
        }

        // Validar que el check-in no sea muy anticipado (ej: máximo 1 año)
        if (ChronoUnit.DAYS.between(LocalDate.now(), checkin) > 365) {
            throw new BadRequestException("No se pueden hacer reservas con más de 1 año de anticipación");
        }
    }

    /**
     * Valida que la propiedad esté disponible en las fechas solicitadas.
     */
    private void validarDisponibilidad(Long propiedadId, LocalDate checkin, LocalDate checkout) {
        validarDisponibilidad(propiedadId, checkin, checkout, null);
    }

    private void validarDisponibilidad(Long propiedadId, LocalDate checkin, LocalDate checkout, Long reservaIdExcluir) {
        List<Reserva> reservasConflictivas = reservaRepository.findReservasConflictivas(
                propiedadId,
                checkin,
                checkout,
                List.of(EstadoReserva.CONFIRMADA, EstadoReserva.PAGADA, EstadoReserva.EN_CURSO)
        );

        // Excluir la reserva actual si se está actualizando
        if (reservaIdExcluir != null) {
            reservasConflictivas = reservasConflictivas.stream()
                    .filter(r -> !r.getId().equals(reservaIdExcluir))
                    .toList();
        }

        if (!reservasConflictivas.isEmpty()) {
            throw new BadRequestException("La propiedad no está disponible en las fechas seleccionadas");
        }
    }

    /**
     * Obtiene el usuario autenticado.
     */
    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    /**
     * Obtiene una reserva por ID.
     */
    private Reserva obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
    }

    /**
     * Convierte una entidad Reserva a ReservaDTO.
     */
    private ReservaDTO convertirADTO(Reserva reserva) {
        ReservaDTO dto = modelMapper.map(reserva, ReservaDTO.class);
        
        // Calcular número de noches
        dto.setNumeroNoches((int) ChronoUnit.DAYS.between(reserva.getFechaCheckin(), reserva.getFechaCheckout()));
        
        // Datos de la propiedad
        dto.setPropiedadId(reserva.getPropiedad().getId());
        dto.setPropiedadTitulo(reserva.getPropiedad().getTitulo());
        dto.setPropiedadDireccion(reserva.getPropiedad().getDireccion());
        dto.setPropiedadCiudad(reserva.getPropiedad().getCiudad());
        dto.setPropiedadImagenPrincipal(
                reserva.getPropiedad().getImagenes() != null && !reserva.getPropiedad().getImagenes().isEmpty()
                        ? reserva.getPropiedad().getImagenes().iterator().next()
                        : null
        );
        
        // Datos del viajero
        dto.setViajeroId(reserva.getViajero().getId());
        dto.setViajeroNombre(reserva.getViajero().getNombreCompleto());
        dto.setViajeroEmail(reserva.getViajero().getEmail());
        dto.setViajeroTelefono(reserva.getViajero().getTelefono());
        
        // Datos del anfitrión
        dto.setAnfitrionId(reserva.getPropiedad().getAnfitrion().getId());
        dto.setAnfitrionNombre(reserva.getPropiedad().getAnfitrion().getNombreCompleto());
        dto.setAnfitrionEmail(reserva.getPropiedad().getAnfitrion().getEmail());
        dto.setAnfitrionTelefono(reserva.getPropiedad().getAnfitrion().getTelefono());
        
        return dto;
    }

    /**
     * Convierte una entidad Reserva a ReservaResumenDTO.
     */
    private ReservaResumenDTO convertirAResumenDTO(Reserva reserva) {
        Usuario usuarioActual = getUsuarioAutenticado();
        boolean esViajero = reserva.getViajero().getId().equals(usuarioActual.getId());
        
        return ReservaResumenDTO.builder()
                .id(reserva.getId())
                .estado(reserva.getEstado())
                .fechaCheckin(reserva.getFechaCheckin())
                .fechaCheckout(reserva.getFechaCheckout())
                .numeroNoches((int) ChronoUnit.DAYS.between(reserva.getFechaCheckin(), reserva.getFechaCheckout()))
                .numeroHuespedes(reserva.getNumeroHuespedes())
                .precioTotal(reserva.getPrecioTotal())
                .propiedadId(reserva.getPropiedad().getId())
                .propiedadTitulo(reserva.getPropiedad().getTitulo())
                .propiedadCiudad(reserva.getPropiedad().getCiudad())
                .propiedadImagenPrincipal(
                        reserva.getPropiedad().getImagenes() != null && !reserva.getPropiedad().getImagenes().isEmpty()
                                ? reserva.getPropiedad().getImagenes().iterator().next()
                                : null
                )
                .contraparteNombre(esViajero 
                        ? reserva.getPropiedad().getAnfitrion().getNombreCompleto()
                        : reserva.getViajero().getNombreCompleto())
                .contraparteEmail(esViajero
                        ? reserva.getPropiedad().getAnfitrion().getEmail()
                        : reserva.getViajero().getEmail())
                .build();
    }
}
