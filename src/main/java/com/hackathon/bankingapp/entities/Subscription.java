package com.hackathon.bankingapp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private double amount;
    private int intervalSeconds;
    private Instant startTime;
    private boolean active = true;

    public Subscription(User user, double amount, int intervalSeconds) {
        this.user = user;
        this.amount = amount;
        this.intervalSeconds = intervalSeconds;
    }
}
