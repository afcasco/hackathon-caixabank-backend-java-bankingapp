package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidCredentialsException;
import com.hackathon.bankingapp.exceptions.InvalidPinException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PinService {

    public static final String USER_NOT_FOUND_FOR_ACCOUNT_NUMBER = "User not found for account number: ";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String INVALID_PIN = "Invalid PIN";


    @Transactional
    public String createPin(UUID accountNumber, String pin, String password) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_FOR_ACCOUNT_NUMBER + accountNumber));

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setHashedPin(passwordEncoder.encode(pin));
        userRepository.save(user);

        return "PIN created successfully";
    }


    @Transactional
    public String updatePin(UUID accountNumber, String oldPin, String password, String newPin) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_FOR_ACCOUNT_NUMBER + accountNumber));

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getHashedPin() == null || !passwordEncoder.matches(oldPin, user.getHashedPin())) {
            throw new InvalidPinException(INVALID_PIN);
        }

        user.setHashedPin(passwordEncoder.encode(newPin));
        userRepository.save(user);

        return "PIN updated successfully";
    }


    public void verifyPin(UUID accountNumber, String pin) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_FOR_ACCOUNT_NUMBER + accountNumber));

        if (user.getHashedPin() == null) {
            throw new InvalidPinException(INVALID_PIN);
        }

        if (!passwordEncoder.matches(pin, user.getHashedPin())) {
            throw new InvalidPinException(INVALID_PIN);
        }
    }
}
