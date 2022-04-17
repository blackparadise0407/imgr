package com.example.imgr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountRegistrationMail {
    private String recipientName;
    private String link;
}
