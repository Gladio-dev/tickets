package com.group.artifName.controllers;

import com.group.artifName.dtos.LoginDto;
import com.group.artifName.dtos.RegisterDto;
import com.group.artifName.dtos.ChangeDto;
import com.group.artifName.dtos.ResetDto;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.User;
import com.group.artifName.services.AuthService;
import com.group.artifName.services.JwtService;
import com.group.artifName.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth") // Todas las rutas empezarán con http://localhost:8080/api/auth
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService; // 1. Agregamos la variable
    private final UserService userService;


    public AuthController(AuthService authService, JwtService jwtService, UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto userRegister, HttpServletRequest request) {
        try {
            User loggedUser = authService.getAuthenticatedUser(request); // Autenticación automática por cookie
            if (!userService.isAdmin(loggedUser)){
                Map<String,String> err = new HashMap<>();
                err.put("error","not admin");
                err.put("message","Solo administradores pueden registrar usuarios");
                return ResponseEntity.badRequest().body(err);
            }
            User registeredUser = authService.register(userRegister);

            // Creamos una respuesta bonita en formato JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("email", userRegister.getEmail());
            response.put("nombre", userRegister.getName());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Si el servicio lanza un error (ej. correo ya existe), lo atrapamos aquí
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
        try {
            User loggedUser = authService.login(loginDto);
            // 1. GENERAR EL JWT REAL CON EL EMAIL Y EL ROL
            String jwtToken = jwtService.generateToken(loggedUser.getEmail(), loggedUser.getRole().name());

            // 2. Guardamos el TOKEN dentro de la cookie AUTH_TOKEN
            Cookie authCookie = new Cookie("AUTH_TOKEN", jwtToken);

            // Configuraciones de seguridad para Next.js
            authCookie.setHttpOnly(true);
            authCookie.setSecure(true);    // Cambiar a 'true' en producción (HTTPS)
            authCookie.setPath("/");

            // 7 días El navegador guardará la cookie en el disco duro y no se borrará al apagar la PC
            authCookie.setMaxAge(6 * 24 * 60 * 60);
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
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Solo eliminar cookie
        response =authService.logOut(response);
        Map<String,String> res = new HashMap<>();
        res.put("message","sesión cerrada correctamente");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/session")
    public ResponseEntity<?> verify(HttpServletRequest request){
        try {
            User logedUser = authService.getAuthenticatedUser(request); // Autenticación automática por cookie

            // Creamos una respuesta bonita en formato JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "Sesión iniciada");
            response.put("email", logedUser.getEmail());
            response.put("role", logedUser.getRole().name());
//            if (logedUser.getActive()|| !logedUser.getNeedNewPassword()) {
//                response.put("Rpass", "false");
//            }else response.put("Rpass", "true");


            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Si el servicio lanza un error (ej. correo ya existe), lo atrapamos aquí
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    @PostMapping("/change_password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangeDto changeDto,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        try {
            // 1. Validar credenciales
            User loggedUser = authService.getAuthenticatedUser(request);
            if(!userService.matchPassword(loggedUser,changeDto.getOldPassword())){
                return ResponseEntity.ok(Map.of(
                        "message", "Contraseña incorrecta"
                ));
            }
            // 2. Resetear contraseña
            authService.changePassword(loggedUser, changeDto.getNewPassword());
            response = authService.logOut(response);
            // 3. Respuesta exitosa
            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña cambiada correctamente, inicia sesion con tu nueva contraseña"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/reset_password")
    public ResponseEntity<?> restartPassword(@Valid @RequestBody ResetDto resetDto, HttpServletRequest request) {
        try {
            // 1. Validar credenciales
            User loggedUser = authService.getAuthenticatedUser(request);


            if (loggedUser.getRole() != Role.ADMIN) {
                Map<String, String> err = new HashMap<>();
                err.put("error", "not admin");
                err.put("message", "Solo un administrador puede resetear contraseñas");
                return ResponseEntity.badRequest().body(err);
            }
            Optional<User> userToReset = userService.findUserById(resetDto.getId());



            // 2. Resetear contraseña a 12345
            if (userToReset.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "usuario no existe",
                        "error","true"
                ));
            }
            authService.changePassword(userToReset.get(), "123456");
            // 3. Respuesta exitosa
            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña restablecida"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAdmins() {
        List<User> admins = userService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }
}