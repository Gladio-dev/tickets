package com.group.artifName.services;

import com.group.artifName.entities.AccountToken;
import com.group.artifName.entities.TokenType;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.AccountTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountTokenService {
    private final AccountTokenRepository accountTokenRepository;

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
        accountToken.setCreatedAt(LocalDateTime.now());
        accountToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
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
        accountToken.setCreatedAt(LocalDateTime.now());
        accountToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        accountToken.setUsed(false);
// FUNCION PARA ENVIAR EL CORREO
        return accountTokenRepository.save(accountToken);
    }

    @Transactional
    public User consumeActivationToken(UUID tokenUuid) {

        AccountToken token = accountTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new RuntimeException("Token de activación no encontrado."));

        if (token.getType() != TokenType.ACTIVATE) {
            throw new RuntimeException("El token no es de activación.");
        }

        if (token.isUsed()) {
            throw new RuntimeException("El token ya fue utilizado.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado.");
        }

        token.setUsed(true);
        token.setUsedAt(LocalDateTime.now());

        return token.getUser();
    }












}
