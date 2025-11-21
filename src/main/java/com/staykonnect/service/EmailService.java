package com.staykonnect.service;

import com.staykonnect.domain.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails.
 * Utiliza JavaMailSender para enviar notificaciones por correo electrónico.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Envía email de verificación de cuenta.
     *
     * @param usuario Usuario que se registró
     */
    @Async
    public void enviarEmailVerificacion(Usuario usuario) {
        String verificationUrl = frontendUrl + "/verificar-email?token=" + usuario.getTokenVerificacion();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(usuario.getEmail());
        message.setSubject("Verificación de cuenta - StayKonnect");
        message.setText(String.format(
                "Hola %s,\n\n" +
                        "Gracias por registrarte en StayKonnect.\n\n" +
                        "Por favor, verifica tu cuenta haciendo clic en el siguiente enlace:\n" +
                        "%s\n\n" +
                        "Si no te registraste en StayKonnect, ignora este correo.\n\n" +
                        "Saludos,\n" +
                        "El equipo de StayKonnect",
                usuario.getNombreCompleto(),
                verificationUrl
        ));

        try {
            mailSender.send(message);
            log.info("Email de verificación enviado a: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a: {}", usuario.getEmail(), e);
        }
    }

    /**
     * Envía email de recuperación de contraseña.
     *
     * @param usuario Usuario que solicitó recuperación
     * @param token Token de recuperación
     */
    @Async
    public void enviarEmailRecuperacionPassword(Usuario usuario, String token) {
        String resetUrl = frontendUrl + "/resetear-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(usuario.getEmail());
        message.setSubject("Recuperación de contraseña - StayKonnect");
        message.setText(String.format(
                "Hola %s,\n\n" +
                        "Has solicitado recuperar tu contraseña.\n\n" +
                        "Para crear una nueva contraseña, haz clic en el siguiente enlace:\n" +
                        "%s\n\n" +
                        "Este enlace expirará en 24 horas.\n\n" +
                        "Si no solicitaste este cambio, ignora este correo.\n\n" +
                        "Saludos,\n" +
                        "El equipo de StayKonnect",
                usuario.getNombreCompleto(),
                resetUrl
        ));

        try {
            mailSender.send(message);
            log.info("Email de recuperación de contraseña enviado a: {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a: {}", usuario.getEmail(), e);
        }
    }
}
