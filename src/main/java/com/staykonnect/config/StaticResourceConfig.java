package com.staykonnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración para servir archivos estáticos (imágenes, documentos).
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos desde el directorio de uploads
        registry.addResourceHandler("/images/**", "/documents/**")
                .addResourceLocations("file:" + uploadDir + "/images/", "file:" + uploadDir + "/documents/");
    }
}
