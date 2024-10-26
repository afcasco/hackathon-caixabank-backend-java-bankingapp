package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.dto.AccountInfoDto;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    public AccountInfoDto getAccountInfoByAccountNumber(String accountNumber) {
        User user = userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));

        return new AccountInfoDto(user.getAccountNumber().toString(), user.getBalance());
    }
}
