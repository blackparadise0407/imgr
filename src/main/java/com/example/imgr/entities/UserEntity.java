package com.example.imgr.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
    @Index(name = "uniqueIndex", columnList = "email", unique = true)
})
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String username;
    private String password;
    private boolean verified = false;

    public UserEntity(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
