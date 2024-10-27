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

    private static final String USER_NOT_FOUND = "User not found";
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
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        validatePin(user, pin);
        checkBalance(user, amount);

        Subscription subscription = new Subscription(user, amount, intervalSeconds);
        subscriptionRepository.save(subscription);
        return "Subscription created successfully.";
    }

    @Transactional
    public String activateAutoInvestBot(UUID userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        validatePin(user, pin);
        activeAutoInvestBots.put(userId, true);
        return "Automatic investment enabled successfully.";
    }

    @Transactional
    @Scheduled(fixedRate = 300)
    public void processSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findByActiveTrue();
        for (Subscription subscription : subscriptions) {
            processSubscription(subscription);
        }
    }

    private void processSubscription(Subscription subscription) {
        User user = subscription.getUser();
        if (!hasSufficientBalance(user, subscription.getAmount())) {
            deactivateSubscription(subscription);
        } else {
            deductAndLogTransaction(user, subscription.getAmount(), TransactionType.SUBSCRIPTION, null);
        }
    }

    private boolean hasSufficientBalance(User user, double amount) {
        return user.getBalance() >= amount;
    }

    private void deactivateSubscription(Subscription subscription) {
        subscription.setActive(false);
        subscriptionRepository.save(subscription);
    }

    private void deductAndLogTransaction(User user, double amount, TransactionType type, String symbol) {
        user.setBalance(user.getBalance() - amount);
        userRepository.save(user);
        logTransaction(user.getAccountNumber(), amount, type, symbol);
    }

    @Transactional
    @Scheduled(fixedRate = 3000)
    public void runAutoInvestBots() {
        activeAutoInvestBots.keySet().forEach(this::processUserInvestments);
    }

    private void processUserInvestments(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        user.getAssets().forEach(asset -> evaluateAssetForInvestment(user, asset));
    }

    private void evaluateAssetForInvestment(User user, UserAsset asset) {
        double currentPrice = marketService.getPriceForSymbol(asset.getSymbol());
        double purchasePrice = asset.getPurchasePrice();

        if (shouldBuyAsset(currentPrice, purchasePrice)) {
            performBuy(user, asset, currentPrice);
        } else if (shouldSellAsset(currentPrice, purchasePrice)) {
            performSell(user, asset, currentPrice);
        }
    }

    private boolean shouldBuyAsset(double currentPrice, double purchasePrice) {
        return currentPrice < purchasePrice * 0.8;
    }

    private boolean shouldSellAsset(double currentPrice, double purchasePrice) {
        return currentPrice > purchasePrice * 1.2;
    }

    private void performBuy(User user, UserAsset asset, double currentPrice) {
        double amountToBuy = 0.1 * user.getBalance();
        if (hasSufficientBalance(user, amountToBuy)) {
            buyAsset(user, asset.getSymbol(), amountToBuy, currentPrice);
            logTransaction(user.getAccountNumber(), amountToBuy, TransactionType.ASSET_PURCHASE, asset.getSymbol());
        }
    }

    private void performSell(User user, UserAsset asset, double currentPrice) {
        double quantityToSell = 0.1 * asset.getQuantity();
        if (quantityToSell > 0) {
            double proceeds = sellAsset(user, asset.getSymbol(), quantityToSell, currentPrice);
            logTransaction(user.getAccountNumber(), proceeds, TransactionType.ASSET_SELL, asset.getSymbol());
        }
    }

    private void logTransaction(UUID accountNumber, double amount, TransactionType type, String symbol) {
        Transaction transaction = new Transaction(null, accountNumber, accountNumber, amount, type, Instant.now(), symbol);
        transactionRepository.save(transaction);
    }

    private void buyAsset(User user, String symbol, double amount, double currentPrice) {
        double quantityToBuy = amount / currentPrice;

        UserAsset asset = userAssetRepository.findByUserAndSymbol(user, symbol).stream().findAny()
                .orElseGet(() -> new UserAsset(null, user, symbol, 0, currentPrice, Instant.now()));

        asset.setQuantity(asset.getQuantity() + quantityToBuy);
        asset.setPurchasePrice(currentPrice);
        userAssetRepository.save(asset);

        deductAndLogTransaction(user, amount, TransactionType.ASSET_PURCHASE, symbol);
    }

    private double sellAsset(User user, String symbol, double quantityToSell, double currentPrice) {
        UserAsset asset = userAssetRepository.findByUserAndSymbol(user, symbol)
                .stream().findAny().orElseThrow(() -> new IllegalArgumentException("User does not own this asset"));

        double proceeds = quantityToSell * currentPrice;

        updateAssetQuantityOrDelete(asset, quantityToSell);

        user.setBalance(user.getBalance() + proceeds);
        userRepository.save(user);

        return proceeds;
    }

    private void updateAssetQuantityOrDelete(UserAsset asset, double quantityToSell) {
        asset.setQuantity(asset.getQuantity() - quantityToSell);
        if (asset.getQuantity() == 0) {
            userAssetRepository.delete(asset);
        } else {
            userAssetRepository.save(asset);
        }
    }

    @Transactional
    public String cancelSubscription(UUID userId, String pin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        validatePin(user, pin);

        List<Subscription> subscriptions = subscriptionRepository.findByUserAndActiveTrue(user);
        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(this::deactivateSubscription);
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

    private void checkBalance(User user, double amount) {
        if (user.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance to create subscription.");
        }
    }
}
