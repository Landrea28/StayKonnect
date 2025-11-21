package com.staykonnect.security;

import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio personalizado para cargar detalles de usuario desde la base de datos.
 * Implementa UserDetailsService de Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su email (username en este contexto).
     *
     * @param email Email del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con email: " + email)
                );

        return UserPrincipal.create(usuario);
    }

    /**
     * Carga un usuario por su ID.
     *
     * @param id ID del usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con id: " + id)
                );

        return UserPrincipal.create(usuario);
    }
}
