// Transaction.java
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
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private UUID sourceAccountNumber;
    private UUID targetAccountNumber;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Instant transactionDate;
}
