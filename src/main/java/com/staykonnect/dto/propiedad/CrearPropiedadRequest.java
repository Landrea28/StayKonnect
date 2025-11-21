package com.staykonnect.dto.propiedad;

import com.staykonnect.domain.enums.TipoPropiedad;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO para la solicitud de creación de propiedad.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPropiedadRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 10, max = 100, message = "El título debe tener entre 10 y 100 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 50, max = 2000, message = "La descripción debe tener entre 50 y 2000 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de propiedad es obligatorio")
    private TipoPropiedad tipoPropiedad;

    // Ubicación
    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres")
    private String ciudad;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede superar los 100 caracteres")
    private String pais;

    @Size(max = 20, message = "El código postal no puede superar los 20 caracteres")
    private String codigoPostal;

    @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "La longitud debe estar entre -180 y 180")
    private Double longitud;

    // Características
    @NotNull(message = "El número de habitaciones es obligatorio")
    @Min(value = 0, message = "El número de habitaciones debe ser mayor o igual a 0")
    @Max(value = 50, message = "El número de habitaciones no puede superar 50")
    private Integer habitaciones;

    @NotNull(message = "El número de camas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 cama")
    @Max(value = 100, message = "El número de camas no puede superar 100")
    private Integer camas;

    @NotNull(message = "El número de baños es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 baño")
    @Max(value = 20, message = "El número de baños no puede superar 20")
    private Integer banos;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1 persona")
    @Max(value = 100, message = "La capacidad no puede superar 100 personas")
    private Integer capacidad;

    @Positive(message = "El área debe ser un valor positivo")
    private Double areaM2;

    // Precios
    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio por noche debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precioPorNoche;

    @DecimalMin(value = "0.00", message = "El precio de limpieza debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal precioLimpieza;

    @DecimalMin(value = "0.00", message = "El depósito de seguridad debe ser mayor o igual a 0")
    @Digits(integer = 10, fraction = 2, message = "El depósito debe tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal depositoSeguridad;

    // Servicios e imágenes
    private List<String> servicios;

    @NotEmpty(message = "Debe incluir al menos una imagen")
    @Size(min = 1, max = 20, message = "Debe incluir entre 1 y 20 imágenes")
    private List<String> imagenes;

    // Reglas y horarios
    @Size(max = 1000, message = "Las reglas de la casa no pueden superar los 1000 caracteres")
    private String reglasCasa;

    @NotNull(message = "La hora de check-in es obligatoria")
    private LocalTime horaCheckin;

    @NotNull(message = "La hora de check-out es obligatoria")
    private LocalTime horaCheckout;

    @Min(value = 1, message = "La estancia mínima debe ser al menos 1 noche")
    @Max(value = 365, message = "La estancia mínima no puede superar 365 noches")
    private Integer estanciaMinima;

    @Min(value = 1, message = "La estancia máxima debe ser al menos 1 noche")
    @Max(value = 365, message = "La estancia máxima no puede superar 365 noches")
    private Integer estanciaMaxima;
}
