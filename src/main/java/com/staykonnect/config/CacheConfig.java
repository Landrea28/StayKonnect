package com.staykonnect.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de caché para mejorar el rendimiento
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROPIEDADES_CACHE = "propiedades";
    public static final String USUARIOS_CACHE = "usuarios";
    public static final String RESERVAS_CACHE = "reservas";
    public static final String VALORACIONES_CACHE = "valoraciones";
    public static final String BUSQUEDA_CACHE = "busqueda";
    public static final String ESTADISTICAS_CACHE = "estadisticas";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            PROPIEDADES_CACHE,
            USUARIOS_CACHE,
            RESERVAS_CACHE,
            VALORACIONES_CACHE,
            BUSQUEDA_CACHE,
            ESTADISTICAS_CACHE
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * Cache para propiedades (más tiempo, datos más estables)
     */
    @Bean
    public Caffeine<Object, Object> propiedadesCaffeineConfig() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * Cache para búsquedas (menos tiempo, queries dinámicas)
     */
    @Bean
    public Caffeine<Object, Object> busquedaCaffeineConfig() {
        return Caffeine.newBuilder()
            .initialCapacity(200)
            .maximumSize(2000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * Cache para estadísticas (datos que cambian poco)
     */
    @Bean
    public Caffeine<Object, Object> estadisticasCaffeineConfig() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(200)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats();
    }
}
