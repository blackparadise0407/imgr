package com.example.imgr.controllers;

import com.example.imgr.common.ApiError;
import com.example.imgr.common.ApiResponse;
import com.example.imgr.dto.*;
import com.example.imgr.entities.TokenEntity;
import com.example.imgr.entities.UserEntity;
import com.example.imgr.enums.TokenType;
import com.example.imgr.mail.EmailService;
import com.example.imgr.repositories.TokenRepository;
import com.example.imgr.repositories.UserRepository;
import com.example.imgr.security.jwt.JwtUtils;
import com.example.imgr.services.TokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;

    @Value("${imgr.app.token.refresh_expiration}")
    private Long refreshExpirationMs;
    @Value("${imgr.app.token.verify_expiration}")
    private Long verifyExpirationMs;

    @Value("${imgr.app.token.reset_expiration}")
    private Long resetExpirationMs;

    @Value("${imgr.app.redirect_url}")
    private String redirectUrl;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder encoder,
                          EmailService emailService,
                          TokenService tokenService,
                          TokenRepository tokenRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;

    }

    @GetMapping("")
    public String getCurrentUser() {
        return "auth endpoint";

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, @Valid @RequestBody LoginRequest loginRequest) {
        if(!userRepository.existsByEmailOrUsername(loginRequest.getEmail(), loginRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiError("Bad credentials"));
        }

        Optional<UserEntity> user = userRepository.findByEmailOrUsername(loginRequest.getEmail(),loginRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("Bad credentials"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.get().getUsername(), loginRequest.getPassword())
        );

        if (!user.get().isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(HttpStatus.FORBIDDEN, "Your account is not verified"));
        }

        List<TokenEntity> existingTokens = tokenRepository.findAllByUserId(user.get().getId());

        String incomingRequestIp = request.getRemoteHost();
        Timestamp expiredAt = new Timestamp(System.currentTimeMillis() + refreshExpirationMs);

        for (TokenEntity token : existingTokens) {
            if (token.getIpAddress().equals(incomingRequestIp)) {
                token.setExpiredAt(expiredAt);
                tokenRepository.save(token);
                String jwt = jwtUtils.generateJwtToken(authentication);
                return ResponseEntity.ok(new LoginResponse(jwt, user.get(), token.getValue()));
            } else {
                tokenRepository.deleteById(token.getId());
            }
        }

        String jwt = jwtUtils.generateJwtToken(authentication);

        TokenEntity refreshToken = new TokenEntity();
        refreshToken.setValue(tokenService.generateRandomString());
        refreshToken.setIpAddress(incomingRequestIp);
        refreshToken.setUser(user.get());
        refreshToken.setType(TokenType.TOKEN_REFRESH);
        refreshToken.setExpiredAt(expiredAt);
        tokenRepository.save(refreshToken);

        return ResponseEntity.ok(new LoginResponse(jwt, user.get(), refreshToken.getValue()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException, IOException {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiError("Email is already in use"));
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiError("Username is already taken"));
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setUsername(registerRequest.getUsername());
        userRepository.save(user);

        // Create new verify token
        TokenEntity token = tokenService.createEmailVerificationToken(user);

        // Send confirmation email to user
        emailService.sendAccountConfirmationEmail(user.getEmail(),
                new AccountRegistrationMail(user.getUsername(), String.format("http://localhost:8081/api/auth/verify?code=%s", token.getValue())));
        return ResponseEntity.ok(new ApiResponse("Register user successfully"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam(value = "code") String code, HttpServletResponse response) throws IOException {
        Optional<TokenEntity> foundToken = tokenRepository.findByValue(code);

        if (foundToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("This account verification token is invalid"));
        }

        TokenEntity token = foundToken.get();

        if(token.getExpiredAt().before(new Timestamp(System.currentTimeMillis()))) {
            return ResponseEntity.badRequest().body(new ApiError("Your verification link has expired"));
        }

        Optional<UserEntity> foundUser =  userRepository.findById(token.getUser().getId());

        if(foundUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("This account verification token is invalid"));
        }

        UserEntity user = foundUser.get();

        user.setVerified(true);
        userRepository.save(user);

        tokenRepository.delete(token);

        response.sendRedirect(redirectUrl);
        return ResponseEntity.ok(new ApiResponse("Your email has been verified"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest resendVerificationRequest) throws MessagingException, IOException {
        Optional<UserEntity> foundUser = userRepository.findById(resendVerificationRequest.getUserId());

        if(foundUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("User not found"));
        }

        UserEntity user = foundUser.get();

        if(user.isVerified()) {
            return ResponseEntity.badRequest().body(new ApiError("This account is already verified"));
        }

        List<TokenEntity> foundTokens = tokenRepository.findAllByUserId(resendVerificationRequest.getUserId());
        for (TokenEntity token: foundTokens) {
            tokenRepository.delete(token);
        }

        // Create new verify token
        TokenEntity token = tokenService.createEmailVerificationToken(user);
        // Send confirmation email to user
        emailService.sendAccountConfirmationEmail(user.getEmail(),
                new AccountRegistrationMail(user.getUsername(), String.format("http://localhost:8081/api/auth/verify?code=%s", token.getValue())));
        return ResponseEntity.ok(new ApiResponse("New verification link has been sent"));
    }

    @PostMapping("/token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, Authentication authentication,@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String oldAccessToken = jwtUtils.parseJwt(request);

        Optional<TokenEntity> foundRefreshToken = tokenRepository.findByValue(refreshTokenRequest.getRefreshToken());
        if(foundRefreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiError("Invalid refresh token"));
        }

        if(!jwtUtils.getUserIdFromJwtToken(oldAccessToken, true).equals(refreshTokenRequest.getUserId().toString())) {
            return ResponseEntity.badRequest().body(new ApiError("Invalid request, that's all we know"));
        }

        if(!userRepository.existsById(refreshTokenRequest.getUserId())) {
            return ResponseEntity.badRequest().body(new ApiError("Invalid request, that's all we know"));
        }

        TokenEntity refreshToken = foundRefreshToken.get();
        if(refreshToken.getExpiredAt().before(new Timestamp(System.currentTimeMillis()))) {
            return ResponseEntity.badRequest().body(new ApiError("Refresh token has expired"));
        }

        String newAccessToken = jwtUtils.generateJwtToken(refreshTokenRequest.getUserId().toString());

        return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken));
    }
}
