package com.example.minibilling.repository;

import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.model.entity.Role;
import com.example.minibilling.repository.jpa.AccountEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AccountRepository {

    private final AccountEntityRepository accountEntityRepository;

    public AccountRepository(AccountEntityRepository accountEntityRepository) {
        this.accountEntityRepository = accountEntityRepository;
    }

    public List<AccountEntity> findAll() {
        return accountEntityRepository.findAll();
    }

    public AccountEntity createAccount(String username, String encodedPassword, Role role) {
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID().toString().replace("-", ""));
        account.setUsername(username);
        account.setPassword(encodedPassword);
        account.setRole(role);
        return accountEntityRepository.save(account);
    }

    public AccountEntity deleteAccount(String username) {
        AccountEntity account = accountEntityRepository.findByUsername(username);
        if (account == null) {
            return null;
        }
        accountEntityRepository.delete(account);
        return account;
    }
}
