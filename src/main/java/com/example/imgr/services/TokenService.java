package com.example.imgr.services;

import com.example.imgr.entities.TokenEntity;
import com.example.imgr.entities.UserEntity;
import com.example.imgr.enums.TokenType;
import com.example.imgr.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.sql.Timestamp;

@Service
public class TokenService {
    @Value("${imgr.app.token.verify_expiration}")
    private Long verifyExpirationMs;

    static final String alphanumeric = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    public String generateRandomString() {
        return generateRandomString(64);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public TokenEntity createEmailVerificationToken(UserEntity user) {
        String code = generateRandomString();
        TokenEntity token = new TokenEntity();
        token.setType(TokenType.EMAIL_VERIFY);
        token.setValue(code);
        token.setExpiredAt(new Timestamp(System.currentTimeMillis() + verifyExpirationMs));
        token.setUser(user);
        tokenRepository.save(token);
        return token;
    }
}
