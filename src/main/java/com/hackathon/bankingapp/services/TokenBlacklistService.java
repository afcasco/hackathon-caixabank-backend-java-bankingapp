package com.hackathon.bankingapp.services;

import com.hackathon.bankingapp.entities.BlacklistedToken;
import com.hackathon.bankingapp.repositories.BlacklistedTokenRepository;
import com.hackathon.bankingapp.utils.JwtUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

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
        blacklistedTokenRepository.save(new BlacklistedToken(token, expirationTime));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredTokens() {
        blacklistedTokenRepository.deleteByExpirationBefore(Instant.now());
    }

    public boolean isTokenBlacklisted(String token) {
        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findById(token);

        if (blacklistedToken.isPresent()) {
            Instant expirationTime = blacklistedToken.get().getExpiration();
            return Instant.now().isBefore(expirationTime);
        }
        return false;
    }
}
