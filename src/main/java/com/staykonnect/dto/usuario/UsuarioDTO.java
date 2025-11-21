package com.staykonnect.dto.usuario;

import com.staykonnect.domain.enums.EstadoCuenta;
import com.staykonnect.domain.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para la respuesta de información del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String biografia;
    private String fotoPerfil;
    private RolUsuario rol;
    private EstadoCuenta estado;
    private Boolean emailVerificado;
    private Double puntuacionPromedio;
    private Integer totalValoraciones;
}
