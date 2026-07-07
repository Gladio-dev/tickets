package com.group.artifName.services;


import com.group.artifName.dtos.TicketDto;
import com.group.artifName.entities.Role;
import com.group.artifName.entities.Ticket;
import com.group.artifName.entities.TicketStatus;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.TicketRepository;
import com.group.artifName.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isAdmin(User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        return false;
    }

    public boolean matchPassword(User user, String pass){
        return passwordEncoder.matches(pass, user.getPassword());
    }
    public Optional <User> finduserByMail(String mail){
        return userRepository.findByEmail(mail);
    }
}
