package com.staykonnect.infrastructure.security.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Anotación personalizada para inyectar el usuario autenticado en los controladores.
 * Simplifica la obtención del usuario actual desde el contexto de seguridad.
 * 
 * Ejemplo de uso:
 * <pre>
 * {@code
 * @GetMapping("/perfil")
 * public ResponseEntity<?> obtenerPerfil(@CurrentUser UserDetailsImpl userDetails) {
 *     // userDetails contiene la información del usuario autenticado
 *     return ResponseEntity.ok(userDetails);
 * }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
