package com.staykonnect.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspecto para monitorear el performance de métodos críticos
 */
@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    /**
     * Monitorea el tiempo de ejecución de métodos de servicio
     */
    @Around("execution(* com.staykonnect.service.*.*(..))")
    public Object monitorServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 1000) { // Log si toma más de 1 segundo
                log.warn("SLOW QUERY: {}.{} tomó {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("{}.{} ejecutado en {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("ERROR en {}.{} después de {} ms: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                executionTime,
                e.getMessage());
            throw e;
        }
    }

    /**
     * Monitorea queries de repositorio
     */
    @Around("execution(* com.staykonnect.domain.repository.*.*(..))")
    public Object monitorRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (executionTime > 500) { // Log si la query toma más de 500ms
                log.warn("SLOW REPOSITORY QUERY: {}.{} tomó {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    executionTime);
            }
            
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
}
