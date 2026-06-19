package com.group.artifName.repositories;

import com.group.artifName.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Spring leerá esto y generará automáticamente un:
    // SELECT * FROM tickets WHERE user_id = ?
    List<Ticket> findByUserId(Long userId);
}