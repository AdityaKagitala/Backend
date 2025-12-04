package com.example.urlshortener.service;

import com.example.urlshortener.model.User;
import com.example.urlshortener.repository.UserRepository;
import com.example.urlshortener.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public String registerUser(String username,String Email, String password)  {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken!");
        }
        User user = User.builder()
                .username(username)
                .email(Email)
                .password(passwordEncoder.encode(password))

                .build();
        userRepository.save(user);
        return "User registered successfully!";
    }

    public Optional<String> loginUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return Optional.empty();
        }
        return Optional.of(jwtUtil.generateToken(username));
    }
}