package com.group.artifName.services;

import com.group.artifName.config.TimeProvider;
import com.group.artifName.dtos.CreateTicketMessageRequest;
import com.group.artifName.dtos.TicketMessageResponse;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.TicketMessage;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.TicketMessageRepository;
import com.group.artifName.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketMessageService {
    @Autowired
    private TimeProvider timeProvider;
    private final TicketMessageRepository ticketMessageRepository;
    private final TicketRepository ticketRepository;

    public TicketMessage createMessage(
            Long ticketId,
            User sender,
            CreateTicketMessageRequest request
    ) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        TicketMessage message = new TicketMessage();

        message.setContent(request.getContent());
        message.setCreatedAt(timeProvider.now());

        message.setTicket(ticket);
        message.setSender(sender);

        return ticketMessageRepository.save(message);
    }
    public List<TicketMessage> getMessagesByTicket(Long ticketId) {

        return ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

    }



}