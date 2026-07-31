package com.example.minibilling.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    private String id;

    @Column(unique = true)
    private String username;

    @Column(name = "customer_reference")
    private String customerReference;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    public AccountEntity() {}

    public String getCustomerReference() { return customerReference; }
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setCustomerReference(String customerReference) { this.customerReference = customerReference; }
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
