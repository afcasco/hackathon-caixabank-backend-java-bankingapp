package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.User;
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

    public void verifyPin(UUID accountNumber, String pin) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found for the given account number"));

        if (user.getHashedPin() == null || !passwordEncoder.matches(pin, user.getHashedPin())) {
            throw new InvalidPinException("Invalid PIN");
        }
    }


    public String createPin(UUID accountNumber, String pin, String password) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found for the given account number"));

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidPinException("Invalid password for PIN creation");
        }

        user.setHashedPin(passwordEncoder.encode(pin));
        userRepository.save(user);
        return "PIN created successfully";
    }


    public String updatePin(UUID accountNumber, String oldPin, String password, String newPin) {
        User user = userRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found for the given account number"));

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new InvalidPinException("Invalid password for PIN update");
        }

        if (!passwordEncoder.matches(oldPin, user.getHashedPin())) {
            throw new InvalidPinException("Invalid old PIN");
        }

        user.setHashedPin(passwordEncoder.encode(newPin));
        userRepository.save(user);
        return "PIN updated successfully";
    }
}
