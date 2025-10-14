package com.carpool.demo.data.api;
import com.carpool.demo.model.user.User;


public interface UserManager {
    boolean registerUser(String email, String password);
    User login(String email, String password);
    boolean logout(String email);
    boolean emailExists(String email);
    boolean isTokenValid(String token);
}
