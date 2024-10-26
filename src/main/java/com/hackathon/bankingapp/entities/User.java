package com.hackathon.bankingapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID accountNumber;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private double balance;

    @Column(nullable = false)
    private String hashedPassword;

    @Column()
    private String hashedPin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAsset> assets = new ArrayList<>();

    public User(UUID accountNumber, String name, String email, String phoneNumber, String address, double balance, String hashedPassword) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.balance = balance;
        this.hashedPassword = hashedPassword;
    }
}
