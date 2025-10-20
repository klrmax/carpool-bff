package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PropertyFileUserManagerImpl implements UserManager{

    private final String userFile = "src/main/resources-carpool/users.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, User> users = new HashMap<>();

    public PropertyFileUserManagerImpl() {
       loadUsersfromFile();
    }

    private void loadUsersfromFile() {
        try {
            File file = new File(userFile);
            if (file.exists() && file.length() > 0) {
                List<User> userList = mapper.readValue(file, new TypeReference<>() {});
                for (User u : userList) {
                    if (u.getMobileNumber() != null) {
                        users.put(u.getMobileNumber(), u);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Benutzerdatei: " + e.getMessage());
        }
    }
    private void saveUsersToFile(){
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(userFile),
                    users.values()
            );
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Benutzerdatei: " + e.getMessage());
        }
    }
    public User findUserByEmail(String email) {
        return users.get(email);
    }

    @Override
    public boolean registerUser(String mobileNumber, String password, String name) {
        if (users.containsKey(mobileNumber)) {
            System.err.println("User existiert bereits!");
            return false;
        }

        User user = new User();
        user.setMobileNumber(mobileNumber);
        user.setPassword(password);
        user.setName(name);


        users.put(mobileNumber, user);
        saveUsersToFile();

        System.out.println("User wurde erfolgreich registriert !");
        return true;
    }

    @Override
    public User login(String mobileNumber, String password) {
        User user = users.get(mobileNumber);
        if (user != null && user.getPassword().equals(password)) {
            String token = TokenUtils.generateToken();
            user.setToken(token);
            user.setTokenExpiration(System.currentTimeMillis() + (60 * 60 * 1000)); // 1h gültig
            saveUsersToFile();
            return user;
        }
        return null;
    }
    @Override
    public boolean logout(String mobileNumber) {
        User user = users.get(mobileNumber);
        if (user != null) {
            user.setToken(null);
            saveUsersToFile();
            return true;
        }
        return false;
    }
    @Override
    public boolean numberExists(String mobileNumber) {
        return users.containsKey(mobileNumber);
    }
    @Override
    public boolean isTokenValid(String token) {
        return users.values().stream()
                .anyMatch(u -> token.equals(u.getToken()) && u.getTokenExpiration() > System.currentTimeMillis());
    }
}


