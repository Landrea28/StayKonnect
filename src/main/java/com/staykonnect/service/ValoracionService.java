package com.staykonnect.service;

import com.staykonnect.domain.entity.*;
import com.staykonnect.domain.enums.EstadoReserva;
import com.staykonnect.domain.repository.*;
import com.staykonnect.dto.valoracion.*;
import com.staykonnect.exception.BadRequestException;
import com.staykonnect.exception.ForbiddenException;
import com.staykonnect.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final ReservaRepository reservaRepository;
    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Crear valoración de una reserva completada
     */
    @org.springframework.cache.annotation.CacheEvict(
        value = {"estadisticas", "propiedades", "valoraciones"},
        key = "'valoracion-propiedad-' + #reservaId"
    )
    public ValoracionDTO crearValoracion(Long reservaId, Long viajeroId, CrearValoracionRequest request) {
        log.info("Creando valoración para reserva {} por usuario {}", reservaId, viajeroId);

        // Validar que la reserva existe
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el usuario es el viajero de la reserva
        if (!reserva.getViajero().getId().equals(viajeroId)) {
            throw new ForbiddenException("Solo el viajero de la reserva puede valorar");
        }

        // Validar que la reserva está completada
        if (!reserva.getEstado().equals(EstadoReserva.COMPLETADA)) {
            throw new BadRequestException("Solo se pueden valorar reservas completadas");
        }

        // Validar que no existe valoración previa
        if (valoracionRepository.existsByReservaId(reservaId)) {
            throw new BadRequestException("Esta reserva ya ha sido valorada");
        }

        // Validar que el check-out ya pasó (reserva realmente completada)
        if (reserva.getFechaCheckout().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("La reserva aún no ha finalizado");
        }

        // Crear valoración
        Valoracion valoracion = Valoracion.builder()
                .puntuacion(request.getPuntuacion())
                .comentario(request.getComentario())
                .reserva(reserva)
                .valorador(reserva.getViajero())
                .valorado(reserva.getPropiedad().getAnfitrion())
                .propiedad(reserva.getPropiedad())
                .visible(true)
                .build();

        valoracion = valoracionRepository.save(valoracion);

        // Actualizar puntuación promedio de la propiedad
        actualizarPuntuacionPropiedad(reserva.getPropiedad().getId());

        log.info("Valoración {} creada exitosamente", valoracion.getId());
        return convertirAValoracionDTO(valoracion);
    }

    /**
     * Responder a una valoración (solo anfitrión)
     */
    public ValoracionDTO responderValoracion(Long valoracionId, Long anfitrionId, ResponderValoracionRequest request) {
        log.info("Anfitrión {} respondiendo valoración {}", anfitrionId, valoracionId);

        Valoracion valoracion = valoracionRepository.findById(valoracionId)
                .orElseThrow(() -> new ResourceNotFoundException("Valoración no encontrada"));

        // Validar que el usuario es el anfitrión valorado
        if (!valoracion.puedeSerRespondidaPor(anfitrionId)) {
            throw new ForbiddenException("Solo el anfitrión puede responder esta valoración");
        }

        // Validar que no ha respondido previamente
        if (valoracion.tieneRespuesta()) {
            throw new BadRequestException("Esta valoración ya tiene una respuesta");
        }

        valoracion.responder(request.getRespuesta());
        valoracion = valoracionRepository.save(valoracion);

        log.info("Respuesta agregada a valoración {}", valoracionId);
        return convertirAValoracionDTO(valoracion);
    }

    /**
     * Obtener valoraciones de una propiedad
     */
    @Transactional(readOnly = true)
    public Page<ValoracionDTO> obtenerValoracionesPropiedad(Long propiedadId, Pageable pageable) {
        log.info("Obteniendo valoraciones de propiedad {}", propiedadId);

        if (!propiedadRepository.existsById(propiedadId)) {
            throw new ResourceNotFoundException("Propiedad no encontrada");
        }

        Page<Valoracion> valoraciones = valoracionRepository.findByPropiedadIdAndVisibleTrue(
                propiedadId, pageable
        );

        return valoraciones.map(this::convertirAValoracionDTO);
    }

    /**
     * Obtener valoraciones recibidas por un anfitrión
     */
    @Transactional(readOnly = true)
    public Page<ValoracionDTO> obtenerValoracionesAnfitrion(Long anfitrionId, Pageable pageable) {
        log.info("Obteniendo valoraciones de anfitrión {}", anfitrionId);

        if (!usuarioRepository.existsById(anfitrionId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }

        Page<Valoracion> valoraciones = valoracionRepository.findByValoradoIdAndVisibleTrue(
                anfitrionId, pageable
        );

        return valoraciones.map(this::convertirAValoracionDTO);
    }

    /**
     * Obtener valoraciones realizadas por un usuario
     */
    @Transactional(readOnly = true)
    public Page<ValoracionDTO> obtenerMisValoraciones(Long usuarioId, Pageable pageable) {
        log.info("Obteniendo valoraciones realizadas por usuario {}", usuarioId);

        Page<Valoracion> valoraciones = valoracionRepository.findByValoradorId(usuarioId, pageable);
        return valoraciones.map(this::convertirAValoracionDTO);
    }

    /**
     * Obtener valoración de una reserva
     */
    @Transactional(readOnly = true)
    public ValoracionDTO obtenerValoracionPorReserva(Long reservaId, Long usuarioId) {
        log.info("Obteniendo valoración de reserva {} para usuario {}", reservaId, usuarioId);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el usuario está involucrado en la reserva
        boolean esViajero = reserva.getViajero().getId().equals(usuarioId);
        boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(usuarioId);

        if (!esViajero && !esAnfitrion) {
            throw new ForbiddenException("No tienes permiso para ver esta valoración");
        }

        Valoracion valoracion = valoracionRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Esta reserva no tiene valoración"));

        return convertirAValoracionDTO(valoracion);
    }

    /**
     * Obtener estadísticas de valoraciones de una propiedad
     */
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "estadisticas", key = "'valoracion-propiedad-' + #propiedadId")
    public EstadisticasValoracionDTO obtenerEstadisticasPropiedad(Long propiedadId) {
        log.info("Calculando estadísticas de valoraciones para propiedad {}", propiedadId);

        if (!propiedadRepository.existsById(propiedadId)) {
            throw new ResourceNotFoundException("Propiedad no encontrada");
        }

        Double promedio = valoracionRepository.calcularPromedioPropiedad(propiedadId);
        Long total = valoracionRepository.contarValoracionesPropiedad(propiedadId);
        Object[][] distribucion = valoracionRepository.obtenerDistribucionPuntuaciones(propiedadId);

        // Convertir distribución a Map
        Map<Integer, Long> distribucionMap = new HashMap<>();
        for (Object[] row : distribucion) {
            distribucionMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }

        // Calcular porcentajes
        EstadisticasValoracionDTO estadisticas = EstadisticasValoracionDTO.builder()
                .promedioGeneral(promedio != null ? promedio : 0.0)
                .totalValoraciones(total)
                .distribucionPuntuaciones(distribucionMap)
                .build();

        if (total > 0) {
            estadisticas.setPorcentaje5Estrellas(calcularPorcentaje(distribucionMap.getOrDefault(5, 0L), total));
            estadisticas.setPorcentaje4Estrellas(calcularPorcentaje(distribucionMap.getOrDefault(4, 0L), total));
            estadisticas.setPorcentaje3Estrellas(calcularPorcentaje(distribucionMap.getOrDefault(3, 0L), total));
            estadisticas.setPorcentaje2Estrellas(calcularPorcentaje(distribucionMap.getOrDefault(2, 0L), total));
            estadisticas.setPorcentaje1Estrella(calcularPorcentaje(distribucionMap.getOrDefault(1, 0L), total));
        }

        return estadisticas;
    }

    /**
     * Obtener valoraciones pendientes de respuesta (anfitrión)
     */
    @Transactional(readOnly = true)
    public Page<ValoracionDTO> obtenerValoracionesPendientesRespuesta(Long anfitrionId, Pageable pageable) {
        log.info("Obteniendo valoraciones pendientes de respuesta para anfitrión {}", anfitrionId);

        Page<Valoracion> valoraciones = valoracionRepository.findValoracionesPendientesRespuesta(
                anfitrionId, pageable
        );

        return valoraciones.map(this::convertirAValoracionDTO);
    }

    /**
     * Verificar si una reserva puede ser valorada
     */
    @Transactional(readOnly = true)
    public boolean puedeValorarReserva(Long reservaId, Long usuarioId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        boolean esViajero = reserva.getViajero().getId().equals(usuarioId);
        boolean estaCompletada = reserva.getEstado().equals(EstadoReserva.COMPLETADA);
        boolean checkoutPasado = reserva.getFechaCheckout().isBefore(LocalDateTime.now());
        boolean noValorada = !valoracionRepository.existsByReservaId(reservaId);

        return esViajero && estaCompletada && checkoutPasado && noValorada;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void actualizarPuntuacionPropiedad(Long propiedadId) {
        Double promedio = valoracionRepository.calcularPromedioPropiedad(propiedadId);
        
        if (promedio != null) {
            Propiedad propiedad = propiedadRepository.findById(propiedadId)
                    .orElseThrow(() -> new ResourceNotFoundException("Propiedad no encontrada"));
            
            propiedad.setPuntuacionPromedio(promedio);
            propiedadRepository.save(propiedad);
            
            log.debug("Puntuación de propiedad {} actualizada a {}", propiedadId, promedio);
        }
    }

    private Double calcularPorcentaje(Long cantidad, Long total) {
        if (total == 0) return 0.0;
        return (cantidad.doubleValue() / total.doubleValue()) * 100.0;
    }

    private ValoracionDTO convertirAValoracionDTO(Valoracion valoracion) {
        return ValoracionDTO.builder()
                .id(valoracion.getId())
                .puntuacion(valoracion.getPuntuacion())
                .comentario(valoracion.getComentario())
                .respuestaAnfitrion(valoracion.getRespuestaAnfitrion())
                .fechaRespuesta(valoracion.getFechaRespuesta())
                .visible(valoracion.getVisible())
                .createdDate(valoracion.getCreatedDate())
                // Reserva
                .reservaId(valoracion.getReserva().getId())
                .fechaCheckin(valoracion.getReserva().getFechaCheckin())
                .fechaCheckout(valoracion.getReserva().getFechaCheckout())
                // Valorador
                .valoradorId(valoracion.getValorador().getId())
                .valoradorNombre(valoracion.getValorador().getNombre() + " " + valoracion.getValorador().getApellido())
                .valoradorFotoPerfil(valoracion.getValorador().getFotoPerfil())
                // Valorado
                .valoradoId(valoracion.getValorado().getId())
                .valoradoNombre(valoracion.getValorado().getNombre() + " " + valoracion.getValorado().getApellido())
                .valoradoFotoPerfil(valoracion.getValorado().getFotoPerfil())
                // Propiedad
                .propiedadId(valoracion.getPropiedad().getId())
                .propiedadTitulo(valoracion.getPropiedad().getTitulo())
                .propiedadImagenPrincipal(valoracion.getPropiedad().getImagenPrincipal())
                .build();
    }
}
