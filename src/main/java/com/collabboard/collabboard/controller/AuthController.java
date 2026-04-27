package com.collabboard.collabboard.controller;

import com.collabboard.collabboard.dto.AuthRequest;
import com.collabboard.collabboard.dto.AuthResponse;
import com.collabboard.collabboard.entity.User;
import com.collabboard.collabboard.repository.UserRepository;
import com.collabboard.collabboard.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        if (userRepo.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setEmail(req.getEmail());
        user.setHashedPassword(passwordEncoder.encode(req.getPassword()));
        user.setDisplayName(req.getDisplayName());

        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getId(),
                                       user.getEmail(), user.getDisplayName()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        return userRepo.findByEmail(req.getEmail())
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getHashedPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getId(), u.getEmail());
                    return ResponseEntity.ok(new AuthResponse(token, u.getId(),
                                                              u.getEmail(), u.getDisplayName()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}