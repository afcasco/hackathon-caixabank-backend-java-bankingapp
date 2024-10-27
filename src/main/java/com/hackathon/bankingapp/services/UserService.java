package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.dto.LoginRequestDto;
import com.hackathon.bankingapp.dto.UserRegistrationDto;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidCredentialsException;
import com.hackathon.bankingapp.exceptions.UserAlreadyExistsException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import com.hackathon.bankingapp.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User registerUser(UserRegistrationDto registrationDto) {
        validateUserUniqueness(registrationDto);
        return createUser(registrationDto);
    }

    public String loginUser(LoginRequestDto loginRequest) {
        User user = findUserByIdentifier(loginRequest.getIdentifier());
        validatePassword(loginRequest.getPassword(), user.getHashedPassword());
        return jwtUtil.generateToken(user.getAccountNumber().toString());
    }

    public User getUserInfoByAccountNumber(String accountNumber) {
        return userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));
    }

    private void validateUserUniqueness(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists.");
        }
    }

    private User createUser(UserRegistrationDto registrationDto) {
        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());
        User user = new User(UUID.randomUUID(), registrationDto.getName(), registrationDto.getEmail(),
                 registrationDto.getPhoneNumber(), registrationDto.getAddress(), 0.0, hashedPassword);
        return userRepository.save(user);
    }

    private User findUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UserNotFoundException("User not found for the given email: " + identifier));
        } else {
            try {
                UUID accountNumber = UUID.fromString(identifier);
                return userRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new UserNotFoundException("User not found for the given account number: " + identifier));
            } catch (IllegalArgumentException e) {
                throw new UserNotFoundException("User not found for the given account number: " + identifier);
            }
        }
    }


    private void validatePassword(String rawPassword, String hashedPassword) {
        if (!passwordEncoder.matches(rawPassword, hashedPassword)) {
            throw new InvalidCredentialsException();
        }
    }

}
