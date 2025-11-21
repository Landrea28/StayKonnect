package com.staykonnect.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuración general de la aplicación
 */
@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
