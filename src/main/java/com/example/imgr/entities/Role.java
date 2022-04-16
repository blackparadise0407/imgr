package com.example.imgr.entities;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    private String authority;
}
