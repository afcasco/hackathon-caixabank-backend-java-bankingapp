package com.hackathon.bankingapp.repositories;

import com.hackathon.bankingapp.entities.Subscription;
import com.hackathon.bankingapp.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserAndActiveTrue(User user);

    List<Subscription> findByActiveTrue();
}
