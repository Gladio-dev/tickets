package com.group.artifName.controllers;

import com.group.artifName.dtos.TicketDto;
import com.group.artifName.dtos.UpdateTicketStatusDto;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.UserRepository;
import com.group.artifName.services.AuthService;
import com.group.artifName.services.JwtService;
import com.group.artifName.services.TicketService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;
    private final JwtService jwtService; // 1. Inyectamos JwtService
    private final AuthService authservice;

    public TicketController(TicketService ticketService, UserRepository userRepository, JwtService jwtService, AuthService authservice) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authservice = authservice;
    }



    // 1. CREAR TICKET
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TicketDto request, HttpServletRequest httpRequest) {
        try {
            User user = authservice.getAuthenticatedUser(httpRequest); // Autenticación automática por cookie
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
            User user = authservice.getAuthenticatedUser(httpRequest);
            List<Ticket> tickets = ticketService.getTicketsForUser(user);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            Map<String,String> res = new HashMap<>();
            res.put("message",e.getMessage());

            return ResponseEntity.status(401).body("res");
        }
    }

    // 3. CAMBIAR ETAPA - SOLO ADMIN
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateTicketStatusDto request,
                                          HttpServletRequest httpRequest) {
        try {
            User user = authservice.getAuthenticatedUser(httpRequest);
            Ticket updatedTicket = ticketService.updateTicketStatus(id, request.getStatus(), user);
            return ResponseEntity.ok(updatedTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}