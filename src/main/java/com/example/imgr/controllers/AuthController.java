package com.example.imgr.controllers;

import com.example.imgr.dto.AdvancedError;
import com.example.imgr.dto.JwtResponse;
import com.example.imgr.dto.LoginRequest;
import com.example.imgr.dto.RegisterRequest;
import com.example.imgr.entities.UserEntity;
import com.example.imgr.repositories.UserRepository;
import com.example.imgr.security.jwt.JwtUtils;
import com.example.imgr.security.services.UserDetailsImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Log4j2
public class AuthController {

    private AuthenticationManager authenticationManager;

    private JwtUtils jwtUtils;
    private UserRepository userRepository;

    private PasswordEncoder encoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository, PasswordEncoder encoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @GetMapping("")
    public String getCurrentUser() {
        return "auth endpoint";

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails));

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new AdvancedError("Email is already in use!"));
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new AdvancedError("Username is already taken!"));
        }

        UserEntity user = new UserEntity(registerRequest.getEmail(), registerRequest.getUsername(), encoder.encode(registerRequest.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new AdvancedError("Register user successfully!"));

    }
}
