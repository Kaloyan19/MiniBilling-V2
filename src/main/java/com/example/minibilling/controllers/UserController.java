package com.example.minibilling.controllers;

import com.example.minibilling.model.domain.User;
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


    public UserController(UserService service) {
        this.service = service;
    }


    @GetMapping("/info")
    public ResponseEntity<List<User>> getUserInfo() {
        return ResponseEntity.ok(service.findAll());
    }

}
