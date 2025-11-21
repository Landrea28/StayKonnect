package com.staykonnect.controller;

import com.staykonnect.common.dto.ApiResponse;
import com.staykonnect.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para upload de archivos (imágenes y documentos).
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Archivos", description = "Endpoints para subida de archivos")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Sube una imagen.
     *
     * @param file Archivo de imagen
     * @return URL de la imagen subida
     */
    @PostMapping("/upload-image")
    @PreAuthorize("hasAnyRole('ANFITRION', 'VIAJERO', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Subir imagen", description = "Sube una imagen y retorna su URL")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        
        String url = fileStorageService.guardarImagen(file);
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        
        return ResponseEntity.ok(ApiResponse.success("Imagen subida exitosamente", response));
    }

    /**
     * Sube múltiples imágenes.
     *
     * @param files Archivos de imágenes
     * @return URLs de las imágenes subidas
     */
    @PostMapping("/upload-images")
    @PreAuthorize("hasAnyRole('ANFITRION', 'VIAJERO', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Subir múltiples imágenes", description = "Sube múltiples imágenes y retorna sus URLs")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> uploadImages(
            @RequestParam("files") List<MultipartFile> files) {
        
        List<String> urls = fileStorageService.guardarImagenes(files);
        Map<String, List<String>> response = new HashMap<>();
        response.put("urls", urls);
        
        return ResponseEntity.ok(ApiResponse.success("Imágenes subidas exitosamente", response));
    }

    /**
     * Sube un documento.
     *
     * @param file Archivo de documento
     * @return URL del documento subido
     */
    @PostMapping("/upload-document")
    @PreAuthorize("hasAnyRole('ANFITRION', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Subir documento", description = "Sube un documento y retorna su URL")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        
        String url = fileStorageService.guardarDocumento(file);
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        
        return ResponseEntity.ok(ApiResponse.success("Documento subido exitosamente", response));
    }
}
