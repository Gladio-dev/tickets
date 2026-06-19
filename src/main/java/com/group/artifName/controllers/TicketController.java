package com.group.artifName.controllers;

import com.group.artifName.dtos.TicketRequest;
import com.group.artifName.dtos.UpdateStatusRequest;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.UserRepository;
import com.group.artifName.services.JwtService;
import com.group.artifName.services.TicketService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final JwtService jwtService; // 1. Inyectamos JwtService

    public TicketController(TicketService ticketService, UserRepository userRepository, JwtService jwtService) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Método utilitario privado para extraer y validar el usuario desde la cookie
    private User getAuthenticatedUser(HttpServletRequest request) {
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

    // 1. CREAR TICKET (Ya no usa @RequestHeader manual)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TicketRequest request, HttpServletRequest httpRequest) {
        try {
            User user = getAuthenticatedUser(httpRequest); // Autenticación automática por cookie
            Ticket newTicket = ticketService.createTicket(request, user);
            return ResponseEntity.ok(newTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 2. LISTAR TICKETS SEGÚN ROL
    @GetMapping
    public ResponseEntity<?> getAll(HttpServletRequest httpRequest) {
        try {
            User user = getAuthenticatedUser(httpRequest);
            List<Ticket> tickets = ticketService.getTicketsForUser(user);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 3. CAMBIAR ETAPA - SOLO ADMIN
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateStatusRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            User user = getAuthenticatedUser(httpRequest);
            Ticket updatedTicket = ticketService.updateTicketStatus(id, request.getStatus(), user);
            return ResponseEntity.ok(updatedTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}