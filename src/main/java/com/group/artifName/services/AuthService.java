package com.group.artifName.services;

import com.group.artifName.dtos.LoginRequest;
import com.group.artifName.dtos.RegisterRequest;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyección de dependencias por constructor (Buena práctica)
    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(RegisterRequest request) {
        // 1. Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Crear el nuevo usuario y encriptar su contraseña
        User user = new User();
        user.setEmail(request.getEmail());
        // BCrypt transforma "123456" en algo ilegible como "$2a$10$vX..." por seguridad
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. Asignar el rol (Si no viene un rol válido, por defecto es USER)
        try {
            if (request.getRole() != null) {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } else {
                user.setRole(Role.USER);
            }
        } catch (IllegalArgumentException e) {
            user.setRole(Role.USER); // Si mandan un rol que no existe, lo hacemos USER
        }

        // 4. Guardar en la base de datos
        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
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
}