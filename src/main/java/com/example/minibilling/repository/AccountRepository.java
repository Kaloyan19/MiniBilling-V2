package com.example.minibilling.repository;

import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.repository.jpa.AccountEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountRepository {

    private final AccountEntityRepository accountEntityRepository;

    public AccountRepository(AccountEntityRepository accountEntityRepository) {
        this.accountEntityRepository = accountEntityRepository;
    }

    public List<AccountEntity> findAll() {
        return accountEntityRepository.findAll();
    }
}
