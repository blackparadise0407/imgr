package com.example.imgr.repositories;

import com.example.imgr.entities.TokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends CrudRepository<TokenEntity, Long> {
    List<TokenEntity> findAllByUserId(Long id);

    Optional<TokenEntity> findByValue(String value);
}
