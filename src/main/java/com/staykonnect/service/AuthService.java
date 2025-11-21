package com.staykonnect.service;

import com.staykonnect.common.BusinessException;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.EstadoCuenta;
import com.staykonnect.domain.repository.UsuarioRepository;
import com.staykonnect.dto.auth.*;
import com.staykonnect.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de autenticación y gestión de usuarios.
 * Maneja registro, login, recuperación de contraseña y verificación de email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final NotificacionService notificacionService;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Datos del registro
     * @return Respuesta con token JWT
     * @throws BusinessException si el email ya está registrado o las contraseñas no coinciden
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario con email: {}", request.getEmail());

        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmarPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        // Verificar que el email no esté registrado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        // Validar edad mínima (18 años)
        if (request.getFechaNacimiento().plusYears(18).isAfter(LocalDateTime.now().toLocalDate())) {
            throw new BusinessException("Debe ser mayor de 18 años para registrarse");
        }

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .biografia(request.getBiografia())
                .rol(request.getRol())
                .estado(EstadoCuenta.PENDIENTE_VERIFICACION)
                .emailVerificado(false)
                .tokenVerificacion(UUID.randomUUID().toString())
                .puntuacionPromedio(0.0)
                .totalValoraciones(0)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado exitosamente con ID: {}", usuario.getId());

        // Enviar email de verificación
        emailService.enviarEmailVerificacion(usuario);
        
        // Crear notificación de bienvenida
        notificacionService.crearNotificacion(
            usuario.getId(),
            com.staykonnect.domain.enums.TipoNotificacion.REGISTRO_EXITOSO,
            "¡Bienvenido a StayKonnect!",
            String.format("Hola %s, tu cuenta ha sido creada exitosamente. Por favor verifica tu email para activar tu cuenta.", usuario.getNombre()),
            null
        );

        // Autenticar al usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .emailVerificado(usuario.getEmailVerificado())
                .build();
    }

    /**
     * Autentica un usuario con email y contraseña.
     *
     * @param request Credenciales de login
     * @return Respuesta con token JWT
     * @throws BadCredentialsException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login para usuario: {}", request.getEmail());

        // Verificar que el usuario exista
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        // Verificar estado de la cuenta
        if (usuario.getEstado() == EstadoCuenta.BLOQUEADA) {
            throw new BusinessException("La cuenta está bloqueada. Contacte al soporte.");
        }

        if (usuario.getEstado() == EstadoCuenta.INACTIVA) {
            throw new BusinessException("La cuenta está inactiva. Por favor, reactive su cuenta.");
        }

        // Autenticar
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        log.info("Login exitoso para usuario: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol())
                .emailVerificado(usuario.getEmailVerificado())
                .build();
    }

    /**
     * Verifica el email del usuario mediante el token enviado por correo.
     *
     * @param token Token de verificación
     * @throws BusinessException si el token es inválido o expiró
     */
    @Transactional
    public void verificarEmail(String token) {
        log.info("Verificando email con token: {}", token);

        Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new BusinessException("Token de verificación inválido"));

        usuario.setEmailVerificado(true);
        usuario.setEstado(EstadoCuenta.ACTIVA);
        usuario.setTokenVerificacion(null);

        usuarioRepository.save(usuario);
        log.info("Email verificado exitosamente para usuario: {}", usuario.getEmail());
        
        // Notificar verificación exitosa
        notificacionService.crearNotificacion(
            usuario.getId(),
            com.staykonnect.domain.enums.TipoNotificacion.EMAIL_VERIFICADO,
            "Email verificado",
            "Tu email ha sido verificado exitosamente. Ya puedes usar todas las funciones de StayKonnect.",
            null
        );
    }

    /**
     * Inicia el proceso de recuperación de contraseña enviando un email.
     *
     * @param request Email del usuario
     */
    @Transactional
    public void solicitarRecuperacionPassword(RecoverPasswordRequest request) {
        log.info("Solicitud de recuperación de contraseña para: {}", request.getEmail());

        usuarioRepository.findByEmail(request.getEmail()).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setTokenRecuperacion(token);
            usuarioRepository.save(usuario);

            emailService.enviarEmailRecuperacionPassword(usuario, token);
            log.info("Email de recuperación enviado a: {}", request.getEmail());
        });

        // No revelar si el email existe o no por seguridad
    }

    /**
     * Resetea la contraseña usando el token de recuperación.
     *
     * @param request Datos del reseteo
     * @throws BusinessException si el token es inválido o las contraseñas no coinciden
     */
    @Transactional
    public void resetearPassword(ResetPasswordRequest request) {
        log.info("Reseteando contraseña con token: {}", request.getToken());

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new BusinessException("Las contraseñas no coinciden");
        }

        Usuario usuario = usuarioRepository.findByTokenRecuperacion(request.getToken())
                .orElseThrow(() -> new BusinessException("Token de recuperación inválido o expirado"));

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setTokenRecuperacion(null);

        usuarioRepository.save(usuario);
        log.info("Contraseña reseteada exitosamente para usuario: {}", usuario.getEmail());
    }
}
