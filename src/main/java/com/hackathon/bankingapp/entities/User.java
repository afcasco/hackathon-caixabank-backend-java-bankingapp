package com.hackathon.bankingapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String password;
    private String phoneNumber;
    private String address;
    private double balance;

    @Column(nullable = false)
    private String hashedPassword;
}
