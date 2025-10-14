package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.TokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PropertyFileUserManagerImpl implements UserManager{

    private final File userFile = new File("src/main/resources/users.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, User> users = new HashMap<>();

    public PropertyFileUserManagerImpl() {
        try {
            if (userFile.exists()) {
                User[] loadedUsers = mapper.readValue(userFile, User[].class);
                for (User u : loadedUsers) users.put(u.getEmail(), u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveUsers() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(userFile, users.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean registerUser(String email, String password) {
        if (users.containsKey(email)) return false;
        User user = new User();
        users.put(email, user);
        saveUsers();
        return true;
    }
    @Override
    public User login(String email, String password) {
        User user = users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            String token = TokenUtils.generateToken();
            user.setToken(token);
            user.setTokenExpiration(System.currentTimeMillis() + (60 * 60 * 1000)); // 1h gültig
            saveUsers();
            return user;
        }
        return null;
    }
    @Override
    public boolean logout(String email) {
        User user = users.get(email);
        if (user != null) {
            user.setToken(null);
            saveUsers();
            return true;
        }
        return false;
    }
    @Override
    public boolean emailExists(String email) {
        return users.containsKey(email);
    }
    @Override
    public boolean isTokenValid(String token) {
        return users.values().stream()
                .anyMatch(u -> token.equals(u.getToken()) && u.getTokenExpiration() > System.currentTimeMillis());
    }
}


