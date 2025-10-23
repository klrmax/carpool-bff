package com.carpool.demo.data.impl;

import com.carpool.demo.model.user.User;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.data.api.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostgresUserManagerImpl implements UserManager {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User registerUser(User user) {
        // Optional: prüfen, ob Nutzer schon existiert
        if (userRepository.findByMobileNumber(user.getMobileNumber()) != null) {
            throw new RuntimeException("User already exists");
        }
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
        System.out.println("Password check: " + password.equals(user.getPassword()));

        if (user.getPassword().equals(password)) {
            user.setToken(UUID.randomUUID().toString());
            user.setTokenExpiration(System.currentTimeMillis() + 3600000);
            System.out.println("Generated token: " + user.getToken());
            return userRepository.save(user);
        }

        throw new RuntimeException("Invalid credentials");
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
