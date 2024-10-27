package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.dto.AccountInfoDto;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import com.hackathon.bankingapp.entities.UserAsset;
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
        User user = userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));

        return new AccountInfoDto(user.getAccountNumber().toString(), user.getBalance());
    }

    public double getNetWorth(String accountNumber) {
        User user = findUserByAccountNumber(accountNumber);

        double assetWorth = user.getAssets().stream()
                .mapToDouble(asset -> {
                    double currentMarketPrice = marketService.getPriceForSymbol(asset.getSymbol());
                    return asset.getQuantity() * currentMarketPrice;
                })
                .sum();

        return user.getBalance() + assetWorth;
    }


    private User findUserByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));
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


}
