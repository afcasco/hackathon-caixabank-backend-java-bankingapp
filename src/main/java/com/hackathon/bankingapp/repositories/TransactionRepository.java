package com.hackathon.bankingapp.repositories;

import com.hackathon.bankingapp.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountNumber(UUID accountNumber);
}
