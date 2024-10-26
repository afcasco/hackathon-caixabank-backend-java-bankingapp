package com.hackathon.bankingapp.repositories;

import com.hackathon.bankingapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>{

    Optional<User> findByEmail(String email);

    Optional<User> findByAccountNumber(UUID accountNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}