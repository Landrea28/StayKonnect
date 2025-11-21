package com.staykonnect.controller;

import com.staykonnect.common.ApiResponse;
import com.staykonnect.dto.auth.*;
import com.staykonnect.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de autenticación.
 * Maneja registro, login, verificación de email y recuperación de contraseña.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro, login y recuperación de contraseña")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario.
     *
     * @param request Datos del registro
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta nueva de viajero, anfitrión o admin")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario registrado exitosamente. Por favor, verifica tu email.", response));
    }

    /**
     * Autentica un usuario.
     *
     * @param request Credenciales de login
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario con email y contraseña")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    /**
     * Verifica el email del usuario.
     *
     * @param token Token de verificación
     * @return Mensaje de confirmación
     */
    @GetMapping("/verify-email")
    @Operation(summary = "Verificar email", description = "Verifica la cuenta mediante el token enviado por email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verificarEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verificado exitosamente. Tu cuenta está activa.", null));
    }

    /**
     * Solicita recuperación de contraseña.
     *
     * @param request Email del usuario
     * @return Mensaje de confirmación
     */
    @PostMapping("/recover-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía un email con el token de recuperación")
    public ResponseEntity<ApiResponse<Void>> recoverPassword(@Valid @RequestBody RecoverPasswordRequest request) {
        authService.solicitarRecuperacionPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Si el email está registrado, recibirás un correo con las instrucciones.", null));
    }

    /**
     * Resetea la contraseña con el token.
     *
     * @param request Datos del reseteo
     * @return Mensaje de confirmación
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Resetear contraseña", description = "Establece una nueva contraseña usando el token de recuperación")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetearPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Contraseña actualizada exitosamente. Ya puedes iniciar sesión.", null));
    }
}
