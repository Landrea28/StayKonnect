package com.staykonnect.service;

import com.staykonnect.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para manejo de archivos (imágenes de propiedades, documentos, etc.).
 * Gestiona el almacenamiento local de archivos.
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Value("${file.max-size:5242880}") // 5MB por defecto
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );

    public FileStorageService(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Directorio de almacenamiento creado en: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new BusinessException("No se pudo crear el directorio de almacenamiento de archivos", ex);
        }
    }

    /**
     * Guarda una imagen y retorna su URL.
     *
     * @param file Archivo a guardar
     * @return URL del archivo guardado
     */
    public String guardarImagen(MultipartFile file) {
        validarImagen(file);
        return guardarArchivo(file, "images");
    }

    /**
     * Guarda múltiples imágenes.
     *
     * @param files Archivos a guardar
     * @return Lista de URLs de archivos guardados
     */
    public List<String> guardarImagenes(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(guardarImagen(file));
        }
        return urls;
    }

    /**
     * Guarda un documento y retorna su URL.
     *
     * @param file Archivo a guardar
     * @return URL del archivo guardado
     */
    public String guardarDocumento(MultipartFile file) {
        validarDocumento(file);
        return guardarArchivo(file, "documents");
    }

    /**
     * Guarda un archivo en el sistema de archivos.
     *
     * @param file Archivo a guardar
     * @param subdirectorio Subdirectorio donde guardar
     * @return URL del archivo guardado
     */
    private String guardarArchivo(MultipartFile file, String subdirectorio) {
        try {
            // Crear subdirectorio si no existe
            Path targetLocation = this.fileStorageLocation.resolve(subdirectorio);
            Files.createDirectories(targetLocation);

            // Generar nombre único
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String fileName = UUID.randomUUID() + fileExtension;

            // Guardar archivo
            Path filePath = targetLocation.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Archivo guardado: {}", fileName);

            // Retornar URL relativa
            return "/" + subdirectorio + "/" + fileName;
        } catch (IOException ex) {
            log.error("Error al guardar archivo", ex);
            throw new BusinessException("Error al guardar el archivo: " + file.getOriginalFilename(), ex);
        }
    }

    /**
     * Elimina un archivo del sistema.
     *
     * @param fileUrl URL del archivo a eliminar
     */
    public void eliminarArchivo(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }

            // Remover el "/" inicial si existe
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = this.fileStorageLocation.resolve(relativePath).normalize();

            // Verificar que el archivo esté dentro del directorio permitido
            if (!filePath.startsWith(this.fileStorageLocation)) {
                log.warn("Intento de eliminar archivo fuera del directorio permitido: {}", fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado: {}", fileUrl);
        } catch (IOException ex) {
            log.error("Error al eliminar archivo: {}", fileUrl, ex);
        }
    }

    /**
     * Valida que el archivo sea una imagen válida.
     *
     * @param file Archivo a validar
     */
    private void validarImagen(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("El archivo excede el tamaño máximo permitido de " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("Tipo de archivo no permitido. Solo se aceptan imágenes JPEG, PNG o WebP");
        }
    }

    /**
     * Valida que el archivo sea un documento válido.
     *
     * @param file Archivo a validar
     */
    private void validarDocumento(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo está vacío");
        }

        if (file.getSize() > maxFileSize * 2) { // 10MB para documentos
            throw new BusinessException("El archivo excede el tamaño máximo permitido de " + (maxFileSize * 2 / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new BusinessException("Tipo de archivo no permitido. Solo se aceptan PDF o imágenes");
        }
    }
}
