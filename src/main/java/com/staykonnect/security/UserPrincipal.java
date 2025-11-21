package com.staykonnect.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.staykonnect.domain.entity.Usuario;
import com.staykonnect.domain.enums.EstadoCuenta;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementación de UserDetails para integración con Spring Security.
 * Representa el usuario autenticado con sus credenciales y autoridades.
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    private boolean emailVerificado;
    private EstadoCuenta estadoCuenta;

    /**
     * Crea un UserPrincipal desde una entidad Usuario.
     *
     * @param usuario Entidad Usuario de la base de datos
     * @return UserPrincipal con la información del usuario
     */
    public static UserPrincipal create(Usuario usuario) {
        // Convertir el rol del usuario a GrantedAuthority
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new UserPrincipal(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singletonList(authority),
                usuario.getEmailVerificado(),
                usuario.getEstado()
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return estadoCuenta != EstadoCuenta.BLOQUEADA;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return estadoCuenta == EstadoCuenta.ACTIVA;
    }
}
