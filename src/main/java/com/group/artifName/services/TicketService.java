package com.group.artifName.services;

import com.group.artifName.dtos.TicketRequest;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.TicketStatus;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // 1. Crear un ticket (Asociándolo al usuario que inició sesión)
    public Ticket createTicket(TicketRequest request, User user) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.ABIERTO); // Todo ticket nuevo empieza ABIERTO
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUser(user); // Vinculamos el ticket con el usuario

        return ticketRepository.save(ticket);
    }

    // 2. Obtener tickets según el Rol del usuario
    public List<Ticket> getTicketsForUser(User user) {
        // REGLA: Si es ADMIN, ve todos. Si es USER, solo ve los suyos.
        if (user.getRole().equals( Role.ADMIN)) {
            return ticketRepository.findAll();
        } else {
            return ticketRepository.findByUserId(user.getId());
        }
    }

    // 3. Cambiar de etapa (Solo permitido para ADMIN)
    public Ticket updateTicketStatus(Long ticketId, String newStatusStr, User user) {
        // REGLA DE SEGURIDAD: Validar que sea ADMIN
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Acceso denegado: Solo los administradores pueden cambiar el estado");
        }

        // Buscar el ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // Validar y mapear el string al Enum
        try {
            TicketStatus newStatus = TicketStatus.valueOf(newStatusStr.toUpperCase());
            ticket.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de ticket no válido. Use: ABIERTO, EN_PROCESO o RESUELTO");
        }

        return ticketRepository.save(ticket);
    }
}