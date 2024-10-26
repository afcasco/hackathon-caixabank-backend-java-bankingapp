package com.hackathon.bankingapp.repositories;

import com.hackathon.bankingapp.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;


public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    void deleteByExpirationBefore(Instant instant);

}