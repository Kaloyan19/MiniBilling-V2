package com.example.minibilling.controllers;

import com.example.minibilling.model.auth.NewUser;
import com.example.minibilling.model.entity.AccountEntity;
import com.example.minibilling.repository.AccountRepository;
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
    private final AccountRepository accountRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AccountEntityRepository accountRepository, AccountRepository accountRepo, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.accountRepo = accountRepo;
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

    @PostMapping("/createUser")
    public ResponseEntity<String> createUser(@RequestBody NewUser newUser) {
        if (newUser == null) {
            return ResponseEntity.badRequest().build();
        }

        accountRepo.createAccount(newUser.name(), passwordEncoder.encode(newUser.password()), newUser.role().equals("ADMIN") ? com.example.minibilling.model.entity.Role.ADMIN : com.example.minibilling.model.entity.Role.USER);

        return ResponseEntity.ok("User created successfully: " + newUser.name() + "-" + newUser.role());
    }

    @PostMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        AccountEntity deletedAccount = accountRepo.deleteAccount(username);
        if (deletedAccount == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok("User deleted successfully: " + deletedAccount.getUsername());
    }
}
