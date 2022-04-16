package com.example.imgr.dto;

import com.example.imgr.security.services.UserDetailsImpl;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private UserDetailsImpl user;
}
