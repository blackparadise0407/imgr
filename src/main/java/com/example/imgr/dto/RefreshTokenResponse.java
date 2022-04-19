package com.example.imgr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class RefreshTokenResponse {
    private String accessToken;
}
