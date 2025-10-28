package com.carpool.demo.utils;

import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private final UserRepository userRepository;

    public AuthUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Kein Token übergeben");
        }

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new IllegalArgumentException("Ungültiger oder abgelaufener Token");
        }

        return user;
    }
}
