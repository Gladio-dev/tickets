package com.group.artifName.repositories;

import com.group.artifName.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring es tan inteligente que al ver "findByEmail",
    // él mismo creará el SQL: "SELECT * FROM users WHERE email = ?"
    Optional<User> findByEmail(String email);

    // Este nos servirá para el registro: saber si el correo ya existe antes de crearlo
    boolean existsByEmail(String email);
}