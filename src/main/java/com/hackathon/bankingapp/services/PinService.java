package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidCredentialsException;
import com.hackathon.bankingapp.exceptions.InvalidPinException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String createPin(UUID accountNumber, String pin, String password) {
        User user = getUser(accountNumber);

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setPin(passwordEncoder.encode(pin));
        userRepository.save(user);
        return "PIN created successfully";
    }

    public String updatePin(UUID accountNumber, String oldPin, String password, String newPin) {
        User user = getUser(accountNumber);

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(oldPin, user.getPin())) {
            throw new InvalidPinException("Invalid old PIN.");
        }

        user.setPin(passwordEncoder.encode(newPin));
        userRepository.save(user);
        return "PIN updated successfully";
    }

    private User getUser(UUID accountNumber) {
        return userRepository.findById(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));
    }
}
