package com.staykonnect.service;

import com.staykonnect.domain.entity.Notificacion;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.TipoNotificacion;
import com.staykonnect.domain.repository.NotificacionRepository;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.notificacion.NotificacionDTO;
import com.staykonnect.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de notificaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Crea y envía una notificación
     */
    @Transactional
    @Async
    public void crearNotificacion(Long usuarioId, TipoNotificacion tipo, String titulo, String mensaje, String enlace) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            // Crear notificación
            Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .enlace(enlace)
                .leida(false)
                .build();
            
            notificacion = notificacionRepository.save(notificacion);
            log.info("Notificación creada: ID={}, Tipo={}, Usuario={}", 
                    notificacion.getId(), tipo, usuarioId);
            
            // Enviar notificación en tiempo real (WebSocket)
            enviarNotificacionTiempoReal(usuario.getEmail(), convertirADTO(notificacion));
            
            // Enviar email si es necesario
            if (notificacion.requiereEmail()) {
                enviarNotificacionPorEmail(usuario, notificacion);
                notificacion.setEnviadaEmail(true);
                notificacionRepository.save(notificacion);
            }
            
        } catch (Exception e) {
            log.error("Error al crear notificación: {}", e.getMessage(), e);
        }
    }

    /**
     * Lista todas las notificaciones del usuario autenticado
     */
    @Transactional(readOnly = true)
    public Page<NotificacionDTO> listarMisNotificaciones(Pageable pageable) {
        Usuario usuario = obtenerUsuarioActual();
        Page<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdOrderByLeidaAndFecha(usuario.getId(), pageable);
        
        return notificaciones.map(this::convertirADTO);
    }

    /**
     * Cuenta notificaciones no leídas del usuario autenticado
     */
    @Transactional(readOnly = true)
    public long contarNoLeidas() {
        Usuario usuario = obtenerUsuarioActual();
        return notificacionRepository.countNotificacionesNoLeidas(usuario.getId());
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public NotificacionDTO marcarComoLeida(Long notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        
        // Verificar que pertenece al usuario
        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No tienes permiso para modificar esta notificación");
        }
        
        if (!notificacion.getLeida()) {
            notificacion.marcarComoLeida();
            notificacion = notificacionRepository.save(notificacion);
        }
        
        return convertirADTO(notificacion);
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    @Transactional
    public void marcarTodasComoLeidas() {
        Usuario usuario = obtenerUsuarioActual();
        notificacionRepository.marcarTodasComoLeidas(usuario.getId());
        log.info("Todas las notificaciones marcadas como leídas para usuario: {}", usuario.getId());
    }

    /**
     * Elimina una notificación
     */
    @Transactional
    public void eliminarNotificacion(Long notificacionId) {
        Usuario usuario = obtenerUsuarioActual();
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
            .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        
        // Verificar que pertenece al usuario
        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("No tienes permiso para eliminar esta notificación");
        }
        
        notificacionRepository.delete(notificacion);
        log.info("Notificación eliminada: ID={}, Usuario={}", notificacionId, usuario.getId());
    }

    /**
     * Obtiene notificaciones por tipo
     */
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerPorTipo(TipoNotificacion tipo) {
        Usuario usuario = obtenerUsuarioActual();
        List<Notificacion> notificaciones = notificacionRepository
            .findByUsuarioIdAndTipo(usuario.getId(), tipo);
        
        return notificaciones.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de notificaciones
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Usuario usuario = obtenerUsuarioActual();
        
        long total = notificacionRepository.countByUsuarioId(usuario.getId());
        long noLeidas = notificacionRepository.countNotificacionesNoLeidas(usuario.getId());
        List<Object[]> porTipo = notificacionRepository.obtenerEstadisticasPorTipo(usuario.getId());
        
        Map<String, Long> estadisticasTipo = new HashMap<>();
        for (Object[] row : porTipo) {
            estadisticasTipo.put(((TipoNotificacion) row[0]).name(), ((Number) row[1]).longValue());
        }
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total", total);
        estadisticas.put("noLeidas", noLeidas);
        estadisticas.put("leidas", total - noLeidas);
        estadisticas.put("porTipo", estadisticasTipo);
        
        return estadisticas;
    }

    /**
     * Tarea programada para eliminar notificaciones antiguas leídas (más de 30 días)
     */
    @Transactional
    public void limpiarNotificacionesAntiguas() {
        LocalDateTime fecha = LocalDateTime.now().minusDays(30);
        notificacionRepository.eliminarNotificacionesAntiguas(fecha);
        log.info("Notificaciones antiguas eliminadas antes de: {}", fecha);
    }

    // Métodos de utilidad

    /**
     * Envía notificación en tiempo real por WebSocket
     */
    private void enviarNotificacionTiempoReal(String email, NotificacionDTO notificacion) {
        try {
            messagingTemplate.convertAndSendToUser(
                email,
                "/queue/notificaciones",
                notificacion
            );
            log.debug("Notificación enviada por WebSocket a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar notificación por WebSocket: {}", e.getMessage());
        }
    }

    /**
     * Envía notificación por email
     */
    @Async
    private void enviarNotificacionPorEmail(Usuario usuario, Notificacion notificacion) {
        try {
            String asunto = notificacion.getTitulo();
            String cuerpo = construirCuerpoEmail(notificacion);
            
            emailService.enviarEmail(usuario.getEmail(), asunto, cuerpo);
            log.info("Notificación enviada por email a: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar notificación por email: {}", e.getMessage());
        }
    }

    /**
     * Construye el cuerpo del email según el tipo de notificación
     */
    private String construirCuerpoEmail(Notificacion notificacion) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }");
        html.append(".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }");
        html.append(".message { background: white; padding: 20px; border-left: 4px solid #667eea; margin: 20px 0; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }");
        html.append(".footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>StayKonnect</h1>");
        html.append("<h2>").append(notificacion.getTitulo()).append("</h2>");
        html.append("</div>");
        html.append("<div class='content'>");
        html.append("<div class='message'>");
        html.append("<p>").append(notificacion.getMensaje().replace("\n", "<br>")).append("</p>");
        html.append("</div>");
        
        if (notificacion.getEnlace() != null && !notificacion.getEnlace().isEmpty()) {
            html.append("<a href='").append(notificacion.getEnlace()).append("' class='button'>Ver Detalles</a>");
        }
        
        html.append("<div class='footer'>");
        html.append("<p>Has recibido este email porque estás registrado en StayKonnect.</p>");
        html.append("<p>Si no solicitaste esta notificación, puedes ignorar este mensaje.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * Convierte entidad a DTO
     */
    private NotificacionDTO convertirADTO(Notificacion notificacion) {
        return NotificacionDTO.builder()
            .id(notificacion.getId())
            .tipo(notificacion.getTipo())
            .titulo(notificacion.getTitulo())
            .mensaje(notificacion.getMensaje())
            .leida(notificacion.getLeida())
            .fechaLectura(notificacion.getFechaLectura())
            .enlace(notificacion.getEnlace())
            .enviadaEmail(notificacion.getEnviadaEmail())
            .createdDate(notificacion.getCreatedDate())
            .build();
    }

    /**
     * Obtiene el usuario autenticado
     */
    private Usuario obtenerUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    // Contadores adicionales necesarios para el repositorio
    private long countByUsuarioId(Long usuarioId) {
        return notificacionRepository.countByUsuarioId(usuarioId);
    }
}
