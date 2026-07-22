package com.group.artifName.services;

import com.group.artifName.config.TimeProvider;
import com.group.artifName.entities.AccountToken;
import com.group.artifName.entities.TokenType;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.AccountTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class AccountTokenService {
    private final AccountTokenRepository accountTokenRepository;
    @Autowired
    private TimeProvider timeProvider;
    AccountTokenService(AccountTokenRepository accountTokenRepository){
        this.accountTokenRepository = accountTokenRepository;
    }


    public AccountToken registerUser(User user){
        AccountToken accountToken = new AccountToken();
        accountToken.setUser(user);
        while (true){
        UUID uuid = UUID.randomUUID();
        if (accountTokenRepository.findByToken(uuid).isEmpty()){
            accountToken.setToken(uuid);
            break;}
        }
        accountToken.setType(TokenType.ACTIVATE);
        accountToken.setCreatedAt(timeProvider.now());
        accountToken.setExpiresAt(timeProvider.now().plusDays(3));
        accountToken.setUsed(false);
        //Mandar correo de activación



        return accountTokenRepository.save(accountToken);
    }

    public AccountToken resetUser(User user){
        AccountToken accountToken = new AccountToken();
        accountToken.setUser(user);
        while (true){
            UUID uuid = UUID.randomUUID();
            if (accountTokenRepository.findByToken(uuid).isEmpty()){
                accountToken.setToken(uuid);
                break;
            }
        }
        accountToken.setType(TokenType.RESET);
        accountToken.setCreatedAt(timeProvider.now());
        accountToken.setExpiresAt(timeProvider.now().plusHours(24));
        accountToken.setUsed(false);
// FUNCION PARA ENVIAR EL CORREO
        return accountTokenRepository.save(accountToken);
    }

    @Transactional
    public User consumeActivationToken(UUID tokenUuid) {
        AccountToken token = accountTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token de activación no encontrado."));
//        if (token.getType() != TokenType.ACTIVATE) {
//            throw new RuntimeException("El token no es de activación.");
//        }
        if (token.getExpiresAt().isBefore(timeProvider.now())) {
            throw new RuntimeException("El token ha expirado.");
        }
        User user = token.getUser();
        // Eliminar el token en lugar de marcarlo como usado
        accountTokenRepository.delete(token);
        return user;
    }

    @Transactional
    public User consumeResetToken(UUID tokenUuid) {
        AccountToken token = accountTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token de activación no encontrado."));
        if (token.getType() != TokenType.RESET) {
            throw new RuntimeException("El token no es de reseteo.");
        }
        if (token.isUsed()) {
            throw new RuntimeException("El token ya fue utilizado.");
        }
        if (token.getExpiresAt().isBefore(timeProvider.now())) {
            throw new RuntimeException("El token ha expirado.");
        }
        token.setUsed(true);
        token.setUsedAt(timeProvider.now());
        return token.getUser();
    }











}
