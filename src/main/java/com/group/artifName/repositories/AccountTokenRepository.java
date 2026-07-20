package com.group.artifName.repositories;

import com.group.artifName.entities.AccountToken;
import com.group.artifName.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {

    Optional<AccountToken>  findByToken(UUID uuid);
}
