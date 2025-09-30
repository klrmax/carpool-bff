package com.mosbach.demo.data.api;

public interface UserManager {

    boolean createUser(User user);
    String logUserOn(String email, String password);
    boolean logUserOff(String email);
    String getUserEmailFromToken(String token);

}
