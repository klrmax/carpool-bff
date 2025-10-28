package com.carpool.demo.data.impl;

import com.carpool.demo.model.user.User;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
public class PostgresUserManagerImpl implements UserManager {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;


    @Override
    public User registerUser(User user) {
        if (userRepository.findByMobileNumber(user.getMobileNumber()) != null) {
            throw new RuntimeException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User loginUser(String mobileNumber, String password) {
        System.out.println("Login attempt with number: " + mobileNumber);
        User user = userRepository.findByMobileNumber(mobileNumber);

        if (user == null) {
            System.out.println("User not found!");
            throw new RuntimeException("User not found");
        }

        System.out.println("User found: " + user.getName());

        // Passwortprüfung mit BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // JWT generieren
        String token = jwtUtils.generateToken(user.getName(), user.getUserid());
        System.out.println("Generated JWT: " + token);

        // Kein Speichern mehr nötig!
        return user; // nur zurückgeben, nicht speichern
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
}
