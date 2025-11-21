package com.staykonnect.config;

import com.staykonnect.security.CustomUserDetailsService;
import com.staykonnect.security.JwtAuthenticationEntryPoint;
import com.staykonnect.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security con JWT.
 * Define las reglas de autorización, autenticación y filtros personalizados.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura la cadena de filtros de seguridad.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/propiedades", "/api/propiedades/**").permitAll()
                        .requestMatchers("/images/**", "/documents/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Endpoints solo para anfitriones
                        .requestMatchers(HttpMethod.POST, "/api/propiedades").hasRole("ANFITRION")
                        .requestMatchers(HttpMethod.PUT, "/api/propiedades/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/propiedades/**").hasAnyRole("ANFITRION", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/propiedades/**").hasAnyRole("ANFITRION", "ADMIN")

                        // Endpoints de reservas (requieren autenticación y roles específicos)
                        .requestMatchers(HttpMethod.POST, "/api/reservas").hasRole("VIAJERO")
                        .requestMatchers("/api/reservas/**").hasAnyRole("VIAJERO", "ANFITRION", "ADMIN")

                        // Endpoints de pagos
                        .requestMatchers(HttpMethod.POST, "/api/pagos/webhook").permitAll() // Webhook de Stripe
                        .requestMatchers("/api/pagos/**").hasAnyRole("VIAJERO", "ANFITRION", "ADMIN")

                        // Endpoints de mensajería
                        .requestMatchers("/api/mensajes/**").hasAnyRole("VIAJERO", "ANFITRION")
                        
                        // WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll() // WebSocket con autenticación en interceptor

                        // Endpoints de valoraciones
                        .requestMatchers(HttpMethod.GET, "/api/valoraciones/propiedad/**").permitAll() // Públicas
                        .requestMatchers(HttpMethod.GET, "/api/valoraciones/anfitrion/**").permitAll() // Públicas
                        .requestMatchers("/api/valoraciones/**").hasAnyRole("VIAJERO", "ANFITRION")

                        // Endpoints solo para administradores
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                );

        // Agregar filtro JWT antes del filtro de autenticación estándar
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean del proveedor de autenticación.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Bean del gestor de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean del codificador de contraseñas BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
