package com.staykonnect.service;

import com.staykonnect.domain.entity.Propiedad;
import com.staykonnect.domain.repository.PropiedadRepository;
import com.staykonnect.dto.propiedad.BusquedaPropiedadRequest;
import com.staykonnect.dto.propiedad.PropiedadResumenDTO;
import com.staykonnect.specification.PropiedadSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para búsqueda y filtrado avanzado de propiedades.
 * Utiliza JPA Specifications para construir queries dinámicas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusquedaPropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final ModelMapper modelMapper;

    /**
     * Busca propiedades aplicando múltiples filtros dinámicamente.
     *
     * @param request Parámetros de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de propiedades que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public Page<PropiedadResumenDTO> buscarPropiedades(BusquedaPropiedadRequest request, Pageable pageable) {
        log.info("Buscando propiedades con filtros: {}", request);

        // Construir Specification combinando todos los filtros
        Specification<Propiedad> spec = Specification.where(PropiedadSpecification.esActiva())
                .and(PropiedadSpecification.conTexto(request.getQuery()))
                .and(PropiedadSpecification.enCiudad(request.getCiudad()))
                .and(PropiedadSpecification.enPais(request.getPais()))
                .and(PropiedadSpecification.deTipo(request.getTipoPropiedad()))
                .and(PropiedadSpecification.conCapacidadMinima(request.getCapacidadMinima()))
                .and(PropiedadSpecification.conHabitacionesMinimas(request.getHabitacionesMinimas()))
                .and(PropiedadSpecification.conCamasMinimas(request.getCamasMinimas()))
                .and(PropiedadSpecification.conBanosMinimos(request.getBanosMinimos()))
                .and(PropiedadSpecification.enRangoPrecio(request.getPrecioMinimo(), request.getPrecioMaximo()))
                .and(PropiedadSpecification.conServicios(request.getServicios()))
                .and(PropiedadSpecification.conPuntuacionMinima(request.getPuntuacionMinima()))
                .and(PropiedadSpecification.disponibleEntre(request.getFechaInicio(), request.getFechaFin()));

        // Aplicar ordenamiento personalizado si se especifica
        Pageable pageableConOrdenamiento = aplicarOrdenamiento(request, pageable);

        // Ejecutar búsqueda
        Page<Propiedad> propiedades = propiedadRepository.findAll(spec, pageableConOrdenamiento);

        log.info("Búsqueda completada. Resultados encontrados: {}", propiedades.getTotalElements());

        // Convertir a DTOs
        return propiedades.map(this::convertirAResumenDTO);
    }

    /**
     * Aplica ordenamiento personalizado según los parámetros de búsqueda.
     *
     * @param request Request con parámetros de ordenamiento
     * @param pageable Pageable original
     * @return Pageable con ordenamiento aplicado
     */
    private Pageable aplicarOrdenamiento(BusquedaPropiedadRequest request, Pageable pageable) {
        if (request.getOrdenarPor() == null || request.getOrdenarPor().isEmpty()) {
            return pageable;
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(request.getDireccion())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = switch (request.getOrdenarPor().toLowerCase()) {
            case "precio" -> Sort.by(direction, "precioPorNoche");
            case "puntuacion" -> Sort.by(direction, "puntuacionPromedio");
            case "reciente" -> Sort.by(Sort.Direction.DESC, "createdDate");
            case "relevancia" -> Sort.by(Sort.Direction.DESC, "totalValoraciones", "puntuacionPromedio");
            default -> pageable.getSort();
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /**
     * Convierte una entidad Propiedad a PropiedadResumenDTO.
     *
     * @param propiedad Entidad a convertir
     * @return DTO resumido de propiedad
     */
    private PropiedadResumenDTO convertirAResumenDTO(Propiedad propiedad) {
        PropiedadResumenDTO dto = modelMapper.map(propiedad, PropiedadResumenDTO.class);
        dto.setAnfitrionNombre(propiedad.getAnfitrion().getNombreCompleto());
        dto.setImagenPrincipal(propiedad.getImagenes() != null && !propiedad.getImagenes().isEmpty()
                ? propiedad.getImagenes().iterator().next()
                : null);
        return dto;
    }
}
