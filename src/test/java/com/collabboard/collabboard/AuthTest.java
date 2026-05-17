package com.collabboard.collabboard;

import com.collabboard.collabboard.entity.User;
import com.collabboard.collabboard.repository.UserRepository;
import com.collabboard.collabboard.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void passwordHashingWorks() {
        String raw = "password123";
        String hashed = passwordEncoder.encode(raw);
        assertTrue(passwordEncoder.matches(raw, hashed));
        assertFalse(passwordEncoder.matches("wrong", hashed));
    }

    @Test
    void jwtGenerateAndValidate() {
        String token = jwtUtil.generateToken(1L, "test@test.com");
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertEquals("test@test.com", jwtUtil.extractEmail(token));
    }

    @Test
    void saveAndFindUser() {
        User user = new User();
        user.setEmail("ci-" + System.currentTimeMillis() + "@test.com");
        user.setHashedPassword(passwordEncoder.encode("pass"));
        user.setDisplayName("CI User");
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertTrue(userRepository.findByEmail(saved.getEmail()).isPresent());
    }
}