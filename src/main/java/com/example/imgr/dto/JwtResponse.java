package com.example.imgr.dto;

import com.example.imgr.entities.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class JwtResponse {
    private String accessToken;
    private UserEntity user;
}
