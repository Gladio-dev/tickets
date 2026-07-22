package com.group.artifName.services;

import com.group.artifName.config.TimeProvider;
import com.group.artifName.dtos.TicketDto;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.TicketStatus;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.TicketRepository;
import com.group.artifName.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired
    private TimeProvider timeProvider;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository ;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    // 1. Crear un ticket (Asociándolo al usuario que inició sesión)
    public Ticket createTicket(TicketDto request, User user) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.ABIERTO); // Todo ticket nuevo empieza ABIERTO
        ticket.setCreatedAt(timeProvider.now());
        ticket.setUser(user); // Vinculamos el ticket con el usuario
        ticket.setArea(request.getArea());
        ticket.setType(request.getType());

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

    // 2. Obtener tickets por ID
    public Ticket getTicket(Long id) {
        // REGLA: Si es ADMIN, ve todos. Si es USER, solo ve los suyos.
    Optional<Ticket> ticket = ticketRepository.findById(id);

    if (ticket.isPresent()){
        return ticket.get();
    }
      throw new RuntimeException("Ticket no encontrado");
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

    public Ticket assignTicket(Long ticketId, Long assignToId, User user) {
        // REGLA DE SEGURIDAD: Validar que sea ADMIN
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Acceso denegado: Solo los administradores pueden cambiar el estado");
        }

        // Buscar el ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // Validar y mapear el string al Enum
        try {
            Optional<User> assignedTo = userRepository.findById(assignToId);
            if (assignedTo.isEmpty()) {
                throw new IllegalArgumentException();
            }
            ticket.setAssignedTo(assignedTo.get());
            ticket.setAssignedAt(timeProvider.now());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("No se pudo asinar el ticket al usuario seleccionado");
        }

        return ticketRepository.save(ticket);
    }


    public Ticket inProgressTicket(Long ticketId, User user) {

        // Buscar el ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        try {

            if (ticket.getAssignedTo() == null){
                throw new IllegalArgumentException("El ticket no ha sido asignado");
            }
        // Validar y mapear el string al Enum


            if (user.getId() != ticket.getAssignedTo().getId()) {
                throw new IllegalArgumentException();
            }
            ticket.setStatus(TicketStatus.EN_PROCESO);
            ticket.setInProgressAt(timeProvider.now());

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("No se pudo asinar el ticket al usuario seleccionado");
        }
        return ticketRepository.save(ticket);

    }

    public Ticket solveTicket(Long ticketId, User user, String message) {

        // Buscar el ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        try {
            if (ticket.getAssignedTo() == null){
                throw new IllegalArgumentException("El ticket no ha sido asignado");
            }
            if (!(ticket.getStatus() == TicketStatus.EN_PROCESO)){
                throw new IllegalArgumentException("El ticket no ha iniciado proceso");
            }

            ticket.setStatus(TicketStatus.RESUELTO);
            ticket.setSolvedAt(timeProvider.now());
            ticket.setSolution(message);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("No se pudo resolver el ticket seleccionado, "+e.getMessage());
        }
        return ticketRepository.save(ticket);

    }

}