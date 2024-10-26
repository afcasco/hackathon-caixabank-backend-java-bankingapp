package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.dto.LoginRequestDto;
import com.hackathon.bankingapp.dto.UserRegistrationDto;
import com.hackathon.bankingapp.dto.UserResponseDto;
import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.exceptions.InvalidCredentialsException;
import com.hackathon.bankingapp.exceptions.UserAlreadyExistsException;
import com.hackathon.bankingapp.exceptions.UserNotFoundException;
import com.hackathon.bankingapp.repositories.UserRepository;
import com.hackathon.bankingapp.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        checkUserExists(registrationDto);
        User user = createUser(registrationDto);
        userRepository.save(user);
        logger.debug("User registered successfully with email: {}", registrationDto.getEmail());

        return mapToResponseDto(user);
    }

    public String loginUser(LoginRequestDto loginRequest) {
        User user = findUserByIdentifier(loginRequest.getIdentifier());
        checkPassword(loginRequest.getPassword(), user);
        String token = jwtUtil.generateToken(user.getAccountNumber().toString());
        logger.debug("User logged in with identifier: {}", loginRequest.getIdentifier());

        return token;
    }

    public UserResponseDto getUserInfoByAccountNumber(String accountNumber) {
        User user = userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));

        return mapToResponseDto(user);
    }


    private void checkUserExists(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            logger.warn("Attempt to register an already existing email: {}", registrationDto.getEmail());
            throw new UserAlreadyExistsException("Email already exists.");
        }
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            logger.warn("Attempt to register an already existing phone number: {}", registrationDto.getPhoneNumber());
            throw new UserAlreadyExistsException("Phone number already exists.");
        }
    }

    private User createUser(UserRegistrationDto registrationDto) {
        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());
        return new User(
                null,
                registrationDto.getName(),
                registrationDto.getEmail(),
                registrationDto.getPassword(),
                registrationDto.getPhoneNumber(),
                registrationDto.getAddress(),
                0.0,
                hashedPassword
        );
    }

    private User findUserByIdentifier(String identifier) {
        return identifier.contains("@") ?
                userRepository.findByEmail(identifier).orElseThrow(() ->
                        new UserNotFoundException("User not found for the given identifier: " + identifier)) :
                userRepository.findByAccountNumber(UUID.fromString(identifier)).orElseThrow(() ->
                        new UserNotFoundException("User not found for the given identifier: " + identifier));
    }

    private void checkPassword(String password, User user) {
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            logger.warn("Invalid credentials provided for user: {}", user.getEmail());
            throw new InvalidCredentialsException();
        }
    }

    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getAccountNumber().toString(),
                user.getHashedPassword()
        );
    }
}
