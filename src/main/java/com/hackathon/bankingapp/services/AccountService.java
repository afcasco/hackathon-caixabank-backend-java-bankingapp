package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.dto.AccountInfoDto;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final MarketService marketService;

    public AccountInfoDto getAccountInfoByAccountNumber(String accountNumber) {
        User user = findUserByAccountNumber(accountNumber);
        return new AccountInfoDto(user.getAccountNumber().toString(), user.getBalance());
    }

    public double getNetWorth(String accountNumber) {
        User user = findUserByAccountNumber(accountNumber);
        double assetWorth = calculateTotalAssetWorth(user);
        return user.getBalance() + assetWorth;
    }

    public Map<String, Double> getAllAssets(String accountNumber) {
        User user = findUserByAccountNumber(accountNumber);
        return user.getAssets().stream()
                .collect(Collectors.toMap(
                        UserAsset::getSymbol,
                        UserAsset::getQuantity,
                        Double::sum
                ));
    }

    private User findUserByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));
    }

    private double calculateTotalAssetWorth(User user) {
        return user.getAssets().stream()
                .mapToDouble(asset -> asset.getQuantity() * getCurrentMarketPrice(asset.getSymbol()))
                .sum();
    }

    private double getCurrentMarketPrice(String symbol) {
        return marketService.getPriceForSymbol(symbol);
    }
}
