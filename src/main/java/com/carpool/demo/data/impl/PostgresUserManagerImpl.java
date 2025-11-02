package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.exception.GraphQLRequestException;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
            throw new GraphQLRequestException(
                    "Benutzer mit dieser Telefonnummer existiert bereits",
                    ErrorType.BAD_REQUEST
            );
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User loginUser(String mobileNumber, String password) {
        System.out.println("Login attempt with number: " + mobileNumber);
        User user = userRepository.findByMobileNumber(mobileNumber);

        if (user == null) {
            System.out.println("Benutzer nicht gefunden!");
            throw new GraphQLRequestException(
                    "Benutzer wurde nicht gefunden",
                    ErrorType.NOT_FOUND
            );
        }

        System.out.println("Benutzer gefunden: " + user.getName());

        // Passwortprüfung mit BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GraphQLRequestException(
                    "Falsches Passwort",
                    ErrorType.UNAUTHORIZED
            );
        }

        // JWT generieren (optional: wird im Controller verwendet)
        String token = jwtUtils.generateToken(user.getName(), user.getUserid());
        System.out.println("Generated JWT: " + token);

        return user;
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
