package com.example.imgr.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
    @NotNull
    private Long userId;
}
