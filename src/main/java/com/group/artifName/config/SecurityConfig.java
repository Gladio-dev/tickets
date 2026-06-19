package com.group.artifName.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Activamos CORS con nuestra configuración personalizada
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. Deshabilitamos CSRF temporalmente (ya que usaremos cookies HttpOnly con JWT)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        // Le dice a Spring: "No crees ni guardes sesiones en el servidor"
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                )
                // 3. Configurar qué rutas están protegidas y cuáles no
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Permite registro y login sin token
                        .requestMatchers("/api/tickets/**").permitAll()
                        .anyRequest().authenticated()               // Cualquier otra ruta requerirá inicio de sesión
                );

        return http.build();
    }

    // Configuración de CORS para conectar con Next.js
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // REGLA DE ORO: Coloca aquí la URL exacta de tu frontend de Next.js
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeceras permitidas
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // ¡CRUCIAL! Permite que el navegador envíe y reciba cookies entre Next.js y Spring
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}