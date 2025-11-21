package com.staykonnect.service;

import com.staykonnect.domain.entity.Mensaje;
import com.staykonnect.domain.entity.Reserva;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.repository.MensajeRepository;
import com.staykonnect.domain.repository.ReservaRepository;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.mensaje.*;
import com.staykonnect.exception.BadRequestException;
import com.staykonnect.exception.ForbiddenException;
import com.staykonnect.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Enviar mensaje (REST + WebSocket)
     */
    public MensajeDTO enviarMensaje(Long remitenteId, EnviarMensajeRequest request) {
        log.info("Enviando mensaje de usuario {} a usuario {}", remitenteId, request.getDestinatarioId());

        // Validar que no se envíe mensaje a sí mismo
        if (remitenteId.equals(request.getDestinatarioId())) {
            throw new BadRequestException("No puedes enviarte mensajes a ti mismo");
        }

        // Buscar usuarios
        Usuario remitente = usuarioRepository.findById(remitenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario remitente no encontrado"));

        Usuario destinatario = usuarioRepository.findById(request.getDestinatarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destinatario no encontrado"));

        // Validar reserva si se especifica
        Reserva reserva = null;
        if (request.getReservaId() != null) {
            reserva = reservaRepository.findById(request.getReservaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

            // Validar que el usuario esté involucrado en la reserva
            boolean esViajero = reserva.getViajero().getId().equals(remitenteId);
            boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(remitenteId);

            if (!esViajero && !esAnfitrion) {
                throw new ForbiddenException("No estás autorizado para enviar mensajes sobre esta reserva");
            }
        }

        // Crear mensaje
        Mensaje mensaje = Mensaje.builder()
                .contenido(request.getContenido())
                .remitente(remitente)
                .destinatario(destinatario)
                .reserva(reserva)
                .leido(false)
                .build();

        mensaje = mensajeRepository.save(mensaje);

        // Convertir a DTO
        MensajeDTO mensajeDTO = convertirAMensajeDTO(mensaje);

        // Enviar notificación en tiempo real por WebSocket
        enviarNotificacionWebSocket(mensaje);

        log.info("Mensaje {} enviado exitosamente", mensaje.getId());
        return mensajeDTO;
    }

    /**
     * Obtener conversación entre dos usuarios
     */
    @Transactional(readOnly = true)
    public Page<MensajeDTO> obtenerConversacion(Long usuarioId, Long otroUsuarioId, Pageable pageable) {
        log.info("Obteniendo conversación entre usuarios {} y {}", usuarioId, otroUsuarioId);

        // Validar que los usuarios existan
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        if (!usuarioRepository.existsById(otroUsuarioId)) {
            throw new ResourceNotFoundException("El otro usuario no encontrado");
        }

        Page<Mensaje> mensajes = mensajeRepository.findConversacionEntreUsuarios(
                usuarioId, otroUsuarioId, pageable
        );

        return mensajes.map(this::convertirAMensajeDTO);
    }

    /**
     * Obtener todas las conversaciones de un usuario
     */
    @Transactional(readOnly = true)
    public Page<ConversacionDTO> obtenerConversaciones(Long usuarioId, Pageable pageable) {
        log.info("Obteniendo conversaciones del usuario {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Page<Mensaje> ultimosMensajes = mensajeRepository.findUltimosMensajesPorConversacion(
                usuarioId, pageable
        );

        return ultimosMensajes.map(mensaje -> {
            // Determinar quién es el "otro usuario"
            Usuario otroUsuario = mensaje.getRemitente().getId().equals(usuarioId)
                    ? mensaje.getDestinatario()
                    : mensaje.getRemitente();

            // Contar mensajes no leídos con este usuario
            Long noLeidos = mensajeRepository.countMensajesNoLeidosEntreUsuarios(
                    otroUsuario.getId(), usuarioId
            );

            return ConversacionDTO.builder()
                    .otroUsuarioId(otroUsuario.getId())
                    .otroUsuarioNombre(otroUsuario.getNombre() + " " + otroUsuario.getApellido())
                    .otroUsuarioEmail(otroUsuario.getEmail())
                    .otroUsuarioFotoPerfil(otroUsuario.getFotoPerfil())
                    .ultimoMensajeId(mensaje.getId())
                    .ultimoMensajeContenido(mensaje.getContenido())
                    .ultimoMensajeFecha(mensaje.getCreatedDate())
                    .ultimoMensajeLeido(mensaje.getLeido())
                    .yoEnvieUltimoMensaje(mensaje.getRemitente().getId().equals(usuarioId))
                    .mensajesNoLeidos(noLeidos)
                    .build();
        });
    }

    /**
     * Marcar mensaje como leído
     */
    public void marcarComoLeido(Long mensajeId, Long usuarioId) {
        log.info("Marcando mensaje {} como leído por usuario {}", mensajeId, usuarioId);

        Mensaje mensaje = mensajeRepository.findById(mensajeId)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado"));

        // Validar que el usuario sea el destinatario
        if (!mensaje.getDestinatario().getId().equals(usuarioId)) {
            throw new ForbiddenException("Solo el destinatario puede marcar el mensaje como leído");
        }

        if (!mensaje.getLeido()) {
            mensaje.marcarComoLeido();
            mensajeRepository.save(mensaje);

            // Notificar al remitente por WebSocket
            notificarMensajeLeido(mensaje);
        }
    }

    /**
     * Marcar todos los mensajes de una conversación como leídos
     */
    public void marcarConversacionComoLeida(Long usuarioId, Long otroUsuarioId) {
        log.info("Marcando conversación como leída: usuario {} con usuario {}", usuarioId, otroUsuarioId);

        List<Mensaje> mensajesNoLeidos = mensajeRepository.findMensajesNoLeidosDeRemitente(
                otroUsuarioId, usuarioId
        );

        for (Mensaje mensaje : mensajesNoLeidos) {
            mensaje.marcarComoLeido();
            notificarMensajeLeido(mensaje);
        }

        if (!mensajesNoLeidos.isEmpty()) {
            mensajeRepository.saveAll(mensajesNoLeidos);
            log.info("{} mensajes marcados como leídos", mensajesNoLeidos.size());
        }
    }

    /**
     * Contar mensajes no leídos
     */
    @Transactional(readOnly = true)
    public Long contarMensajesNoLeidos(Long usuarioId) {
        return mensajeRepository.countMensajesNoLeidos(usuarioId);
    }

    /**
     * Obtener mensajes de una reserva
     */
    @Transactional(readOnly = true)
    public List<MensajeDTO> obtenerMensajesPorReserva(Long reservaId, Long usuarioId) {
        log.info("Obteniendo mensajes de reserva {} para usuario {}", reservaId, usuarioId);

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Validar que el usuario esté involucrado en la reserva
        boolean esViajero = reserva.getViajero().getId().equals(usuarioId);
        boolean esAnfitrion = reserva.getPropiedad().getAnfitrion().getId().equals(usuarioId);

        if (!esViajero && !esAnfitrion) {
            throw new ForbiddenException("No tienes permiso para ver estos mensajes");
        }

        List<Mensaje> mensajes = mensajeRepository.findByReservaId(reservaId);
        return mensajes.stream()
                .map(this::convertirAMensajeDTO)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void enviarNotificacionWebSocket(Mensaje mensaje) {
        try {
            MensajeWebSocketDTO wsDTO = MensajeWebSocketDTO.builder()
                    .id(mensaje.getId())
                    .contenido(mensaje.getContenido())
                    .fecha(mensaje.getCreatedDate())
                    .remitenteId(mensaje.getRemitente().getId())
                    .remitenteNombre(mensaje.getRemitente().getNombre() + " " + mensaje.getRemitente().getApellido())
                    .remitenteFotoPerfil(mensaje.getRemitente().getFotoPerfil())
                    .destinatarioId(mensaje.getDestinatario().getId())
                    .reservaId(mensaje.getReserva() != null ? mensaje.getReserva().getId() : null)
                    .tipo(MensajeWebSocketDTO.TipoEvento.MENSAJE_NUEVO)
                    .build();

            // Enviar al destinatario específico
            messagingTemplate.convertAndSendToUser(
                    mensaje.getDestinatario().getEmail(), // Username del destinatario
                    "/queue/mensajes",
                    wsDTO
            );

            log.debug("Notificación WebSocket enviada a usuario {}", mensaje.getDestinatario().getEmail());
        } catch (Exception e) {
            log.error("Error al enviar notificación WebSocket: {}", e.getMessage());
        }
    }

    private void notificarMensajeLeido(Mensaje mensaje) {
        try {
            MensajeWebSocketDTO wsDTO = MensajeWebSocketDTO.builder()
                    .id(mensaje.getId())
                    .tipo(MensajeWebSocketDTO.TipoEvento.MENSAJE_LEIDO)
                    .destinatarioId(mensaje.getDestinatario().getId())
                    .build();

            // Notificar al remitente
            messagingTemplate.convertAndSendToUser(
                    mensaje.getRemitente().getEmail(),
                    "/queue/mensajes",
                    wsDTO
            );
        } catch (Exception e) {
            log.error("Error al notificar mensaje leído: {}", e.getMessage());
        }
    }

    private MensajeDTO convertirAMensajeDTO(Mensaje mensaje) {
        return MensajeDTO.builder()
                .id(mensaje.getId())
                .contenido(mensaje.getContenido())
                .leido(mensaje.getLeido())
                .fechaLectura(mensaje.getFechaLectura())
                .createdDate(mensaje.getCreatedDate())
                // Remitente
                .remitenteId(mensaje.getRemitente().getId())
                .remitenteNombre(mensaje.getRemitente().getNombre() + " " + mensaje.getRemitente().getApellido())
                .remitenteEmail(mensaje.getRemitente().getEmail())
                .remitenteFotoPerfil(mensaje.getRemitente().getFotoPerfil())
                // Destinatario
                .destinatarioId(mensaje.getDestinatario().getId())
                .destinatarioNombre(mensaje.getDestinatario().getNombre() + " " + mensaje.getDestinatario().getApellido())
                .destinatarioEmail(mensaje.getDestinatario().getEmail())
                .destinatarioFotoPerfil(mensaje.getDestinatario().getFotoPerfil())
                // Reserva
                .reservaId(mensaje.getReserva() != null ? mensaje.getReserva().getId() : null)
                .reservaTitulo(mensaje.getReserva() != null ? mensaje.getReserva().getPropiedad().getTitulo() : null)
                .build();
    }
}
