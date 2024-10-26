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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists.");
        }
        if (userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new UserAlreadyExistsException("Phone number already exists.");
        }

        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

        User user = new User();
        user.setName(registrationDto.getName());
        user.setEmail(registrationDto.getEmail());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setAddress(registrationDto.getAddress());
        user.setHashedPassword(hashedPassword);

        user = userRepository.save(user);

        return mapToResponseDto(user);
    }

    public String loginUser(LoginRequestDto loginRequest) {
        User user;

        if (loginRequest.getIdentifier().contains("@")) {
            user = userRepository.findByEmail(loginRequest.getIdentifier())
                    .orElseThrow(() ->
                            new UserNotFoundException("User not found for the given identifier: " + loginRequest.getIdentifier()));
        } else {
            user = userRepository.findByAccountNumber(UUID.fromString(loginRequest.getIdentifier()))
                    .orElseThrow(() ->
                            new UserNotFoundException("User not found for the given identifier: " + loginRequest.getIdentifier()));
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtUtil.generateToken(user.getAccountNumber().toString());
    }




    public UserResponseDto getUserInfoByAccountNumber(String accountNumber) {
        User user = userRepository.findByAccountNumber(UUID.fromString(accountNumber))
                .orElseThrow(() -> new UserNotFoundException("User not found for account number: " + accountNumber));

        return mapToResponseDto(user);
    }

    public UserResponseDto mapToResponseDto(User user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setName(user.getName());
        responseDto.setEmail(user.getEmail());
        responseDto.setPhoneNumber(user.getPhoneNumber());
        responseDto.setAddress(user.getAddress());
        responseDto.setAccountNumber(user.getAccountNumber().toString());
        responseDto.setHashedPassword(user.getHashedPassword());
        return responseDto;
    }
}

