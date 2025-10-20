package com.carpool.demo.data.api;
import com.carpool.demo.model.user.User;


public interface UserManager {
    boolean registerUser(String mobileNumber, String password, String name);
    User login(String mobileNumber, String password);
    boolean logout(String email);
    boolean numberExists(String mobileNumber);
    boolean isTokenValid(String token);
}
