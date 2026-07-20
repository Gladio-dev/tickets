package com.group.artifName.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "AccountToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountToken {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true)
    private UUID token;

    @Enumerated(EnumType.STRING)
    private TokenType type;

    private LocalDateTime expiresAt;

    private boolean used;

    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime usedAt;
}