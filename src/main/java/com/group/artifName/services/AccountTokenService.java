package com.group.artifName.services;

import com.group.artifName.entities.AccountToken;
import com.group.artifName.entities.TokenType;
import com.group.artifName.entities.User;
import com.group.artifName.repositories.AccountTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountTokenService {
    private final AccountTokenRepository accountTokenRepository;

    AccountTokenService(AccountTokenRepository accountTokenRepository){
        this.accountTokenRepository = accountTokenRepository;
    }


    public AccountToken activateUser(User user){
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

        return accountTokenRepository.save(accountToken);
    }
}
