package com.hackathon.bankingapp.controllers;

import com.hackathon.bankingapp.dto.AccountInfoDto;
import com.hackathon.bankingapp.dto.UserResponseDto;
import com.hackathon.bankingapp.services.AccountService;
import com.hackathon.bankingapp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final AccountService accountService;

    @GetMapping("/user")
    public ResponseEntity<UserResponseDto> getUserInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String accountNumber = userDetails.getUsername();

        UserResponseDto userInfo = userService.getUserInfoByAccountNumber(accountNumber);
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/account")
    public ResponseEntity<AccountInfoDto> getAccountInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String accountNumber = userDetails.getUsername();

        AccountInfoDto accountInfo = accountService.getAccountInfoByAccountNumber(accountNumber);
        return ResponseEntity.ok(accountInfo);
    }
}