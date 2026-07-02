package com.group.artifName.controllers;

import com.group.artifName.dtos.LoginRequest;
import com.group.artifName.dtos.RegisterRequest;
import com.group.artifName.entities.User;
import com.group.artifName.services.AuthService;
import com.group.artifName.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Todas las rutas empezarán con http://localhost:8080/api/auth
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService; // 1. Agregamos la variable


    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User registeredUser = authService.register(request);

            // Creamos una respuesta bonita en formato JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("email", registeredUser.getEmail());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Si el servicio lanza un error (ej. correo ya existe), lo atrapamos aquí
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            User loggedUser = authService.login(request);

            // 1. GENERAR EL JWT REAL CON EL EMAIL Y EL ROL
            String jwtToken = jwtService.generateToken(loggedUser.getEmail(), loggedUser.getRole().name());

            // 2. Guardamos el TOKEN dentro de la cookie AUTH_TOKEN
            Cookie authCookie = new Cookie("AUTH_TOKEN", jwtToken);

            // Configuraciones de seguridad para Next.js
            authCookie.setHttpOnly(true);
            authCookie.setSecure(false);    // Cambiar a 'true' en producción (HTTPS)
            authCookie.setPath("/");
// 100 años en segundos. El navegador guardará la cookie en el disco duro y no se borrará al apagar la PC
            authCookie.setMaxAge(60 * 60 * 24 * 365 * 100);
            // 3. Inyectamos la cookie en la respuesta HTTP
            response.addCookie(authCookie);

            // 4. Respondemos al frontend con información básica del usuario
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Inicio de sesión correcto");
            body.put("email", loggedUser.getEmail());
            body.put("role", loggedUser.getRole().name());

            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            User loggedUser = authService.login(request);

            // 1. GENERAR EL JWT REAL CON EL EMAIL Y EL ROL
            String jwtToken = jwtService.generateToken(loggedUser.getEmail(), loggedUser.getRole().name());

            // 2. Guardamos el TOKEN dentro de la cookie AUTH_TOKEN
            Cookie authCookie = new Cookie("AUTH_TOKEN", jwtToken);

            // Configuraciones de seguridad para Next.js
            authCookie.setHttpOnly(true);
            authCookie.setSecure(false);    // Cambiar a 'true' en producción (HTTPS)
            authCookie.setPath("/");
// 100 años en segundos. El navegador guardará la cookie en el disco duro y no se borrará al apagar la PC
            authCookie.setMaxAge(60 * 60 * 24 * 365 * 100);
            // 3. Inyectamos la cookie en la respuesta HTTP
            response.addCookie(authCookie);

            // 4. Respondemos al frontend con información básica del usuario
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Inicio de sesión correcto");
            body.put("email", loggedUser.getEmail());
            body.put("role", loggedUser.getRole().name());

            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}