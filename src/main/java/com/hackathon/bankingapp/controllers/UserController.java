package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.LoginRequestDto;
import com.hackathon.bankingapp.dto.LoginResponseDto;
import com.hackathon.bankingapp.dto.UserRegistrationDto;
import com.hackathon.bankingapp.dto.UserResponseDto;
import com.hackathon.bankingapp.mappers.UserMapper;
import com.hackathon.bankingapp.services.TokenBlacklistService;
import com.hackathon.bankingapp.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        String token = userService.loginUser(loginRequest);
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.mapToResponseDto(userService.registerUser(registrationDto)));
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        tokenBlacklistService.blacklistToken(token);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully");
    }
}