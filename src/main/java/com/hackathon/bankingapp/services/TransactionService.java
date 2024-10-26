package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.Transaction;
import com.hackathon.bankingapp.entities.TransactionType;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InsufficientBalanceException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.TransactionRepository;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PinService pinService;

    @Transactional
    public String deposit(UUID accountNumber, String pin, Double amount) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        pinService.verifyPin(accountNumber, pin);

        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);

        Transaction transaction = new Transaction(null, accountNumber, accountNumber, amount, TransactionType.CASH_DEPOSIT, Instant.now());
        transactionRepository.save(transaction);

        return "Cash deposited successfully";
    }

    @Transactional
    public String withdraw(UUID accountNumber, String pin, Double amount) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        pinService.verifyPin(accountNumber, pin);

        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        Transaction transaction = new Transaction(null, accountNumber, accountNumber, amount, TransactionType.CASH_WITHDRAWAL, Instant.now());
        transactionRepository.save(transaction);

        return "Cash withdrawn successfully";
    }

    @Transactional
    public String transfer(UUID sourceAccount, String pin, UUID targetAccount, Double amount) {
        User sender = userRepository.findByAccountNumber(sourceAccount)
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
        User receiver = userRepository.findByAccountNumber(targetAccount)
                .orElseThrow(() -> new UserNotFoundException("Receiver not found"));

        pinService.verifyPin(sourceAccount, pin);

        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        userRepository.save(sender);
        userRepository.save(receiver);

        Transaction transaction = new Transaction(null, sourceAccount, targetAccount, amount, TransactionType.CASH_TRANSFER, Instant.now());
        transactionRepository.save(transaction);

        return "Fund transferred successfully";
    }

    public List<Transaction> getTransactionHistory(UUID accountNumber) {
        return transactionRepository.findBySourceAccountNumber(accountNumber);
    }
}
