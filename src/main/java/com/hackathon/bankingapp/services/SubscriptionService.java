package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.*;
import com.hackathon.bankingapp.exceptions.InsufficientBalanceException;
import com.hackathon.bankingapp.exceptions.InvalidPinException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.SubscriptionRepository;
import com.hackathon.bankingapp.repositories.TransactionRepository;
import com.hackathon.bankingapp.repositories.UserAssetRepository;
import com.hackathon.bankingapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;
    private final TransactionRepository transactionRepository;
    private final MarketService marketService;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<UUID, Boolean> activeAutoInvestBots = new HashMap<>();

    @Transactional
    public String createSubscription(UUID userId, String pin, double amount, int intervalSeconds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validatePin(user, pin);

        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance to create subscription.");
        }

        Subscription subscription = new Subscription(user, amount, intervalSeconds);
        subscriptionRepository.save(subscription);
        return "Subscription created successfully.";
    }

    @Transactional
    public String activateAutoInvestBot(UUID userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validatePin(user, pin);
        activeAutoInvestBots.put(userId, true);
        return "Automatic investment enabled successfully.";
    }

    @Transactional
    @Scheduled(fixedRate = 300)
    public void processSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findByActiveTrue();
        for (Subscription subscription : subscriptions) {
            User user = subscription.getUser();
            if (user.getBalance() < subscription.getAmount()) {
                subscription.setActive(false);
                subscriptionRepository.save(subscription);
                continue;
            }

            user.setBalance(user.getBalance() - subscription.getAmount());
            userRepository.save(user);
            logTransaction(user.getAccountNumber(), subscription.getAmount(), TransactionType.SUBSCRIPTION);
        }
    }

    @Transactional
    @Scheduled(fixedRate = 3000)
    public void runAutoInvestBots() {
        for (UUID userId : activeAutoInvestBots.keySet()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            for (UserAsset asset : user.getAssets()) {
                double currentPrice = marketService.getPriceForSymbol(asset.getSymbol());
                double purchasePrice = asset.getPurchasePrice();
                double quantity = asset.getQuantity();

                if (currentPrice < purchasePrice * 0.8) {
                    double amountToBuy = 0.1 * user.getBalance();
                    if (user.getBalance() >= amountToBuy) {
                        buyAsset(user, asset.getSymbol(), amountToBuy);
                        Transaction transaction = new Transaction(
                                null, user.getAccountNumber(), user.getAccountNumber(), amountToBuy,
                                TransactionType.ASSET_PURCHASE, Instant.now(), asset.getSymbol());
                        transactionRepository.save(transaction);
                    }
                }
                else if (currentPrice > purchasePrice * 1.2) {
                    double quantityToSell = 0.1 * quantity;
                    if (quantityToSell > 0) {
                        double proceeds = sellAsset(user, asset.getSymbol(), quantityToSell);
                        Transaction transaction = new Transaction(
                                null, user.getAccountNumber(), user.getAccountNumber(), proceeds,
                                TransactionType.ASSET_SELL, Instant.now(), asset.getSymbol());
                        transactionRepository.save(transaction);
                    }
                }
            }
        }
    }





    private void logTransaction(UUID accountNumber, double amount, TransactionType type) {
        Transaction transaction = new Transaction(null, accountNumber, accountNumber, amount, type, Instant.now(), null);
        transactionRepository.save(transaction);
    }

    private void buyAsset(User user, String symbol, double amount) {
        double currentPrice = marketService.getPriceForSymbol(symbol);
        double quantityToBuy = amount / currentPrice;

        UserAsset asset = userAssetRepository.findByUserAndSymbol(user, symbol).stream().findAny()
                .orElseGet(() -> new UserAsset(null, user, symbol, 0, currentPrice, Instant.now()));

        asset.setQuantity(asset.getQuantity() + quantityToBuy);
        asset.setPurchasePrice(currentPrice);
        userAssetRepository.save(asset);

        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);

        logTransaction(user.getAccountNumber(), amount, TransactionType.ASSET_PURCHASE);
    }

    private double sellAsset(User user, String symbol, double quantityToSell) {
        UserAsset asset = userAssetRepository.findByUserAndSymbol(user, symbol)
                .stream().findAny().orElseThrow(() -> new IllegalArgumentException("User does not own this asset"));

        double currentPrice = marketService.getPriceForSymbol(symbol);
        double proceeds = quantityToSell * currentPrice;

        asset.setQuantity(asset.getQuantity() - quantityToSell);
        if (asset.getQuantity() == 0) {
            userAssetRepository.delete(asset);
        } else {
            userAssetRepository.save(asset);
        }

        user.setBalance(user.getBalance() + proceeds);
        userRepository.save(user);

        logTransaction(user.getAccountNumber(), proceeds, TransactionType.ASSET_SELL);
        return proceeds;
    }

    @Transactional
    public String cancelSubscription(UUID userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        validatePin(user, pin);

        List<Subscription> subscriptions = subscriptionRepository.findByUserAndActiveTrue(user);
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                subscription.setActive(false);
                subscriptionRepository.save(subscription);
            }
            return "Subscription canceled successfully.";
        } else {
            return "No active subscription found for the user.";
        }
    }

    private void validatePin(User user, String pin) {
        if (!passwordEncoder.matches(pin, user.getHashedPin())) {
            throw new InvalidPinException("Invalid PIN");
        }
    }
}
