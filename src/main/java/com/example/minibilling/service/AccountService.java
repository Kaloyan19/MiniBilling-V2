package com.example.minibilling.service;

import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountEntity> findAll() {
        return accountRepository.findAll();
    }
}
