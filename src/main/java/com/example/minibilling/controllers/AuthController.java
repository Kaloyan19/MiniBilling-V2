package com.example.minibilling.controllers;

import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.repository.jpa.AccountEntityRepository;
import com.example.minibilling.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AccountEntityRepository accountRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AccountEntityRepository accountRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody LoginRequest request) {
        AccountEntity account = accountRepository.findByUsername(request.username());
        if (account == null) {
            return ResponseEntity.status(401).body("Невалидно потребителско име или парола");
        }
        if (!passwordEncoder.matches(request.password(), account.getPassword())){
            return ResponseEntity.status(401).body("Невалидно потребителско име или парола");
        }
        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
