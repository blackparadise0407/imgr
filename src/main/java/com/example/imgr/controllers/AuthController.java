package com.example.imgr.controllers;

import com.example.imgr.common.ApiError;
import com.example.imgr.common.ApiResponse;
import com.example.imgr.dto.AccountRegistrationMail;
import com.example.imgr.dto.JwtResponse;
import com.example.imgr.dto.LoginRequest;
import com.example.imgr.dto.RegisterRequest;
import com.example.imgr.entities.UserEntity;
import com.example.imgr.mail.EmailService;
import com.example.imgr.repositories.UserRepository;
import com.example.imgr.security.jwt.JwtUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {

    private AuthenticationManager authenticationManager;

    private JwtUtils jwtUtils;
    private UserRepository userRepository;

    private PasswordEncoder encoder;
    private EmailService emailService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository, PasswordEncoder encoder, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.emailService = emailService;
    }

    @GetMapping("")
    public String getCurrentUser() {
        return "auth endpoint";

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        if (!userRepository.existsByEmail(loginRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiError("Bad credentials!"));
        }
        Optional<UserEntity> user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiError("Bad credentials!"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.get().getUsername(), loginRequest.getPassword())
        );

        String jwt = jwtUtils.generateJwtToken(authentication);


        return ResponseEntity.ok(new JwtResponse(jwt, user.get()));

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) throws MessagingException, IOException {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiError("Email is already in use!"));
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiError("Username is already taken!"));
        }

        UserEntity user = new UserEntity();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setUsername(registerRequest.getUsername());
        userRepository.save(user);
        emailService.sendSimpleMessage(user.getEmail(), "Account registration", "Register success");

        emailService.sendAccountConfirmationEmail(user.getEmail(), new AccountRegistrationMail(user.getEmail(), "http://localhost:8081/api/auth/verify?code=random"));
        return ResponseEntity.ok(new ApiResponse("Register user successfully!"));
    }
}
