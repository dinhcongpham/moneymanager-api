package com.example.moneymanager.repository;

import com.example.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
    // select from tbl_profiles where email = ?
    Optional<ProfileEntity> findByEmail(String email);

    Optional<ProfileEntity> findByActivationToken(String activationToken);

    boolean existsByEmail(String email);
}
