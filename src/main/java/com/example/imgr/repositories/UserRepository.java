package com.example.imgr.repositories;

import com.example.imgr.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    public Optional<UserEntity> findByEmail(String email);
    public boolean existsByEmail(String email);

    public Optional<UserEntity> findByUsername(String username);
    public boolean existsByUsername(String username);

}

