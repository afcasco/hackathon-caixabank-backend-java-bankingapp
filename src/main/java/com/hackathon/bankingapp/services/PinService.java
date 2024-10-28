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

    private static final String USER_NOT_FOUND_FOR_ACCOUNT_NUMBER = "User not found for account number: ";
    private static final String INVALID_PIN = "Invalid PIN";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String createPin(UUID accountNumber, String pin, String password) {
        User user = fetchUser(accountNumber);
        validatePassword(password, user);

        setNewPin(user, pin);
        return "PIN created successfully";
    }

    @Transactional
    public String updatePin(UUID accountNumber, String oldPin, String password, String newPin) {
        User user = fetchUser(accountNumber);
        validatePassword(password, user);
        validateExistingPin(oldPin, user);

        setNewPin(user, newPin);
        return "PIN updated successfully";
    }

    public void verifyPin(UUID accountNumber, String pin) {
        User user = fetchUser(accountNumber);
        validateExistingPin(pin, user);
    }

    private User fetchUser(UUID accountNumber) {
        return userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_FOR_ACCOUNT_NUMBER + accountNumber));
    }

    private void validatePassword(String password, User user) {
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }
    }

    private void validateExistingPin(String pin, User user) {
        if (user.getHashedPin() == null || !passwordEncoder.matches(pin, user.getHashedPin())) {
            throw new InvalidPinException(INVALID_PIN);
        }
    }

    private void setNewPin(User user, String pin) {
        user.setHashedPin(passwordEncoder.encode(pin));
        userRepository.save(user);
    }
}
