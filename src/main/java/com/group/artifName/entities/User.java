package com.group.artifName.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users") // "user" es palabra reservada en Postgres, por eso usamos "users"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID autoincremental (1, 2, 3...)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password; // Aquí guardaremos la contraseña ENCRIPTADA

    @Enumerated(EnumType.STRING) // Guarda el rol en la BD como texto ("USER", "ADMIN")
    @Column(nullable = false)
    private Role role;

    @Column(nullable = true)
    private String company;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private Boolean active;
}