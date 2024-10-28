package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.BlacklistedToken;
import com.hackathon.bankingapp.repositories.BlacklistedTokenRepository;
import com.hackathon.bankingapp.utils.JwtUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtUtil jwtUtil;

    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository, @Lazy JwtUtil jwtUtil) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public void blacklistToken(String token) {
        Instant expirationTime = jwtUtil.getExpirationDateFromToken(token);
        saveBlacklistedToken(token, expirationTime);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredTokens() {
        deleteExpiredTokens();
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.findById(token)
                .map(this::isTokenStillValid)
                .orElse(false);
    }

    private void saveBlacklistedToken(String token, Instant expirationTime) {
        blacklistedTokenRepository.save(new BlacklistedToken(token, expirationTime));
    }

    private void deleteExpiredTokens() {
        blacklistedTokenRepository.deleteByExpirationBefore(Instant.now());
    }

    private boolean isTokenStillValid(BlacklistedToken blacklistedToken) {
        return Instant.now().isBefore(blacklistedToken.getExpiration());
    }
}
