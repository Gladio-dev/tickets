package com.group.artifName.dtos;

import jakarta.persistence.*;
import com.group.artifName.entities.Role;

public class Userdto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental (1, 2, 3...)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING) // Guarda el rol en la BD como texto ("USER", "ADMIN")
    @Column(nullable = false)
    private Role role ;
}
