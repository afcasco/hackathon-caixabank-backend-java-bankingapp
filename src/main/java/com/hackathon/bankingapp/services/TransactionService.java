package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.Transaction;
import com.hackathon.bankingapp.entities.TransactionType;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import com.hackathon.bankingapp.exceptions.InsufficientBalanceException;
import com.hackathon.bankingapp.exceptions.InsufficientBalanceException400;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.TransactionRepository;
import com.hackathon.bankingapp.repositories.UserAssetRepository;
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
    private final UserAssetRepository userAssetRepository;
    private final PinService pinService;
    private final MarketService marketService;
    private final EmailService emailService;

    @Transactional
    public String deposit(UUID accountNumber, String pin, Double amount) {
        User user = getUserByAccountNumber(accountNumber);
        pinService.verifyPin(accountNumber, pin);
        user.setBalance(user.getBalance() + amount);
        userRepository.save(user);
        saveTransaction(accountNumber, amount, TransactionType.CASH_DEPOSIT, null);
        return "Cash deposited successfully";
    }

    @Transactional
    public String withdraw(UUID accountNumber, String pin, Double amount) {
        User user = getUserByAccountNumber(accountNumber);
        pinService.verifyPin(accountNumber, pin);
        if (user.getBalance() < amount) throw new InsufficientBalanceException400("Insufficient balance");
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        saveTransaction(accountNumber, amount, TransactionType.CASH_WITHDRAWAL, null);
        return "Cash withdrawn successfully";
    }

    @Transactional
    public String transfer(UUID sourceAccount, String pin, UUID targetAccount, Double amount) {
        User sender = getUserByAccountNumber(sourceAccount);
        User receiver = getUserByAccountNumber(targetAccount);
        pinService.verifyPin(sourceAccount, pin);
        if (sender.getBalance() < amount) throw new InsufficientBalanceException400("Insufficient balance");
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);
        userRepository.save(sender);
        userRepository.save(receiver);
        saveTransaction(sourceAccount, amount, TransactionType.CASH_TRANSFER, null);
        return "Fund transferred successfully";
    }

    public String buyAsset(UUID accountNumber, String pin, String assetSymbol, double amountToSpend) {
        User user = getUserByAccountNumber(accountNumber);
        pinService.verifyPin(accountNumber, pin);

        double assetPrice = marketService.getPriceForSymbol(assetSymbol);
        if (assetPrice <= 0) {
            throw new IllegalArgumentException("Invalid asset price for " + assetSymbol);
        }

        double quantityToBuy = Math.round((amountToSpend / assetPrice) * 100.0) / 100.0;

        if (user.getBalance() < amountToSpend) {
            throw new InsufficientBalanceException("Internal error occurred while purchasing the asset.");
        }

        user.setBalance(user.getBalance() - amountToSpend);
        userRepository.save(user);

        UserAsset asset = new UserAsset(null, user, assetSymbol, quantityToBuy, assetPrice, Instant.now());
        userAssetRepository.save(asset);

        saveTransaction(accountNumber, amountToSpend, TransactionType.ASSET_PURCHASE, assetSymbol);

        emailService.sendInvestmentConfirmation(user, assetSymbol, quantityToBuy, amountToSpend, "Investment Purchase Confirmation");

        return "Asset purchase successful";
    }



    @Transactional
    public String sellAsset(UUID accountNumber, String pin, String assetSymbol, double quantityToSell) {
        User user = getUserByAccountNumber(accountNumber);
        pinService.verifyPin(accountNumber, pin);

        List<UserAsset> userAssets = userAssetRepository.findByUserAndSymbol(user, assetSymbol);
        double totalQuantity = userAssets.stream().mapToDouble(UserAsset::getQuantity).sum();
        if (totalQuantity < quantityToSell) {
            throw new InsufficientBalanceException("Internal error occurred while selling the asset.");
        }

        double saleProceeds = quantityToSell * marketService.getPriceForSymbol(assetSymbol);
        double costBasis = 0.0;
        double remainingQuantity = quantityToSell;

        for (UserAsset asset : userAssets) {
            if (asset.getQuantity() <= remainingQuantity) {
                remainingQuantity -= asset.getQuantity();
                costBasis += asset.getQuantity() * asset.getPurchasePrice();
                userAssetRepository.delete(asset);
            } else {
                costBasis += remainingQuantity * asset.getPurchasePrice();
                asset.setQuantity(asset.getQuantity() - remainingQuantity);
                userAssetRepository.save(asset);
                break;
            }
        }

        double profitOrLoss = saleProceeds - costBasis;
        user.setBalance(user.getBalance() + saleProceeds);
        userRepository.save(user);

        saveTransaction(accountNumber, saleProceeds, TransactionType.ASSET_SELL, assetSymbol);
        emailService.sendSaleConfirmation(user, assetSymbol, quantityToSell, profitOrLoss, "Investment Sale Confirmation");

        return "Asset sale successful";
    }


    public List<Transaction> getTransactionHistory(UUID accountNumber) {
        return transactionRepository.findBySourceAccountNumber(accountNumber);
    }

    private User getUserByAccountNumber(UUID accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));
    }

    private void saveTransaction(UUID accountNumber, double amount, TransactionType type, String assetSymbol) {
        transactionRepository.save(new Transaction(null, accountNumber, accountNumber, amount, type, Instant.now(), assetSymbol));
    }
}
