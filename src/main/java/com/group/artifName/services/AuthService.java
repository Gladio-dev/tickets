package com.group.artifName.services;

import com.group.artifName.dtos.LoginDto;
import com.group.artifName.dtos.RegisterDto;
import com.group.artifName.entities.AccountToken;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.group.artifName.services.JwtService;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountTokenService accountTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Inyección de dependencias por constructor (Buena práctica)
    public AuthService(UserRepository userRepository, JwtService jwtService, BCryptPasswordEncoder passwordEncoder,
                       AccountTokenService accountTokenService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.accountTokenService = accountTokenService;
    }

    public User register(RegisterDto request) {
        // 1. Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        // 2. Crear el nuevo usuario y encriptar su contraseña
        User user = new User();
        user.setEmail(request.getEmail());
        // BCrypt transforma "123456" en algo ilegible como "$2a$10$vX..." por seguridad
        user.setCompany(request.getCompany());
        user.setName(request.getName());
        user.setActive(false);
        // 3. Asignar el rol
        user.setRole(Role.USER);
        // 4. Guardar en la base de datos
        User created = userRepository.save(user);
        AccountToken accountToken = accountTokenService.activateUser(created);



        return created;
    }

    public User login(LoginDto request) {
        // 1. Buscar si el usuario existe

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        // 2. Verificar si la contraseña coincide con la encriptada en la BD
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        // Si todo está bien, retornamos el usuario autenticado
        return user;
    }

    // Método utilitario privado para extraer y validar el usuario desde la cookie
    public User getAuthenticatedUser(HttpServletRequest request) {
        // A. Buscar la cookie AUTH_TOKEN
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        // B. Si la cookie no existe, denegar acceso
        if (token == null || !jwtService.isTokenValid(token)) {
            throw new RuntimeException("Acceso denegado: Token inválido o no proporcionado");
        }
        // C. Extraer el email del token y buscar al usuario en la BD
        String email = jwtService.extractEmail(token);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public boolean changePassword(User user, String newPassword) {
        // 1. Hashear la nueva contraseña
        String hashedPassword = passwordEncoder.encode(newPassword);

        // 2. Actualizar el objeto usuario
        user.setPassword(hashedPassword);

        // 3. Guardar en la base de datos
        userRepository.save(user);

        return true;
    }

    public HttpServletResponse logOut(HttpServletResponse response) {

        Cookie cookie = new Cookie("AUTH_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return response;
    }

}

