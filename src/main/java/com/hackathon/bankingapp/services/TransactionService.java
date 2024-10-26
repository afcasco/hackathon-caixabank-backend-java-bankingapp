package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.Transaction;
import com.hackathon.bankingapp.entities.TransactionType;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
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
    private final MarketService marketService;
    private final EmailService emailService;


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

    @Transactional
    public String buyAsset(UUID accountNumber, String pin, String assetSymbol, double amount) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        pinService.verifyPin(accountNumber, pin);

        double assetPrice = marketService.getPriceForSymbol(assetSymbol);
        if (assetPrice == 0.0) throw new IllegalArgumentException("Invalid asset symbol");

        double quantityToBuy = amount / assetPrice;

        if (user.getBalance() < amount) throw new InsufficientBalanceException("Insufficient balance");

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        UserAsset asset = new UserAsset();
        asset.setUser(user);
        asset.setSymbol(assetSymbol);
        asset.setQuantity(quantityToBuy);
        asset.setPurchasePrice(assetPrice);
        asset.setPurchaseDate(Instant.now());

        user.getAssets().add(asset);  // Add to user assets
        userRepository.save(user);

        emailService.sendInvestmentConfirmation(user, assetSymbol, quantityToBuy, amount, "Investment Purchase Confirmation");

        return "Asset purchase successful";
    }


    @Transactional
    public String sellAsset(UUID accountNumber, String pin, String assetSymbol, double quantity) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        pinService.verifyPin(accountNumber, pin);

        double totalAvailableQuantity = user.getAssets().stream()
                .filter(asset -> asset.getSymbol().equals(assetSymbol))
                .mapToDouble(UserAsset::getQuantity)
                .sum();

        if (totalAvailableQuantity < quantity) throw new InsufficientBalanceException("Insufficient asset quantity for sale");

        double currentPrice = marketService.getPriceForSymbol(assetSymbol);
        double saleAmount = quantity * currentPrice;

        user.setBalance(user.getBalance() + saleAmount);

        double remainingQuantityToSell = quantity;
        for (UserAsset asset : user.getAssets()) {
            if (!asset.getSymbol().equals(assetSymbol) || remainingQuantityToSell <= 0) continue;

            double quantityToSell = Math.min(asset.getQuantity(), remainingQuantityToSell);
            asset.setQuantity(asset.getQuantity() - quantityToSell);
            remainingQuantityToSell -= quantityToSell;

            if (asset.getQuantity() == 0) user.getAssets().remove(asset);
        }

        userRepository.save(user);

        emailService.sendInvestmentConfirmation(user, assetSymbol, -quantity, saleAmount, "Investment Sale Confirmation");

        return "Asset sale successful";
    }


    public List<Transaction> getTransactionHistory(UUID accountNumber) {
        return transactionRepository.findBySourceAccountNumber(accountNumber);
    }
}
