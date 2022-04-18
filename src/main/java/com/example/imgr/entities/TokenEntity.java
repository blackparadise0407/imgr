package com.example.imgr.entities;

import com.example.imgr.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String value;

    @Column(nullable = true)
    private String ipAddress;

    @Enumerated(EnumType.ORDINAL)
    private TokenType type;

    private Timestamp expiredAt;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private UserEntity user;

}
