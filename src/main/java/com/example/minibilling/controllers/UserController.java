package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.User;
import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.service.AccountService;
import com.example.minibilling.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;
    private final AccountService accountService;


    public UserController(UserService service, AccountService accountService) {
        this.service = service;
        this.accountService = accountService;
    }


    @GetMapping("/info")
    public ResponseEntity<List<User>> getUserInfo() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountEntity>> getAccounts() {
        return ResponseEntity.ok(accountService.findAll());
    }

}
