package com.example.imgr.services;

import com.example.imgr.entities.UserEntity;
import com.example.imgr.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

}
