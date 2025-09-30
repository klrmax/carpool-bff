package com.mosbach.demo.data.impl;

import com.mosbach.demo.data.api.User;
import com.mosbach.demo.data.api.UserManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PropertyFileUserManagerImpl implements UserManager {

    String fileName;
    List<User> users = new ArrayList<>();
    static PropertyFileUserManagerImpl propertyFileUserManager = null;

    private PropertyFileUserManagerImpl(String fileName) {
        this.fileName = fileName;
    }

    static public PropertyFileUserManagerImpl getPropertyFileUserManagerImpl(String fileName) {
        if (propertyFileUserManager == null)
            propertyFileUserManager = new PropertyFileUserManagerImpl(fileName);
        return propertyFileUserManager;
    }


    void loadProperties() {
        List<User> temp = new ArrayList<>();
        users = new ArrayList<>();
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get(fileName)));
            int i = 1;
            while (properties.containsKey("User." + i + ".email")) {
                temp.add(new UserImpl(
                        properties.getProperty("User." + i + ".name"),
                        properties.getProperty("User." + i + ".email"),
                        properties.getProperty("User." + i + ".password"),
                        properties.getProperty("User." + i + ".token")
                ));
                i++;
            }
            users = temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
            // TODO Add Logger
        }
    }
    void storeProperties() {
        Properties properties = new Properties();
        int i = 1;
        for (User u : users) {
            properties.setProperty("User." + i +".email", u.getEmail());
            properties.setProperty("User." + i +".password", u.getPassword());
            properties.setProperty("User." + i +".name", u.getName());
            properties.setProperty("User." + i +".token", u.getToken());
            i++;
        }
        try {
            properties.store(Files.newOutputStream(Paths.get(fileName)), "Writing all users to properties file");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
            // TODO Add Logger
        }
    }

    @Override
    public boolean createUser(User user) {
        loadProperties();
        boolean found = false;
        for (User u : users)
            if (u.getEmail().equals(user.getEmail()))
                found = true;
        if (!found) {
            user.setToken("OFF");
            users.add(user);
            storeProperties();
            return true;
        }
        return false;
    }


    @Override
    public String logUserOn(String emailFromLogon, String passwordFromLogon) {

        loadProperties();
        boolean found = false;
        String userToken = "OFF";
        Random random = new Random();
        for (User u : users)
            if (u.getEmail().equals(emailFromLogon)) {
                found = true;
                userToken = "" + System.nanoTime() + random.nextInt();
                u.setToken(userToken);
            }
        if (found) {
            storeProperties();
            return userToken;
        }
        return "";
    }

    @Override
    public boolean logUserOff(String email) {

        loadProperties();
        boolean found = false;
        for (User u : users)
            if (u.getEmail().equals(email)) {
                found = true;
                u.setToken("OFF");
            }
        if (found) {
            storeProperties();
            return true;
        }
        return false;
    }

    @Override
    public String getUserEmailFromToken(String tokenFromLogon) {
        loadProperties();
        String userEmail = "";
        for (User u : users)
            if (u.getToken().equals(tokenFromLogon)) {
                userEmail = u.getEmail();
            }
        return userEmail;
    }
}
