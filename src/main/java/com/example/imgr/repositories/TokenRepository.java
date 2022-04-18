package com.example.imgr.repositories;

import com.example.imgr.entities.TokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends CrudRepository<TokenEntity, Long> {
    List<TokenEntity> findAllByUserId(Long id);
}
