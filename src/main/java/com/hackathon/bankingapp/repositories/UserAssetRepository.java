package com.hackathon.bankingapp.repositories;

import com.hackathon.bankingapp.entities.User;
import com.hackathon.bankingapp.entities.UserAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAssetRepository extends JpaRepository<UserAsset, UUID> {
    List<UserAsset> findByUserAndSymbol(User user, String symbol);
}
