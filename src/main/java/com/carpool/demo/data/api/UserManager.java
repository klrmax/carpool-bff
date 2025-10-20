package com.carpool.demo.data.api;

import com.carpool.demo.model.user.User;
import java.util.List;



public interface UserManager {
    User registerUser(User user);
    User loginUser(String mobileNumber, String password);
    List<User> getAllUsers();
    User getUserById(Integer id);
    void deleteUser(Integer id);
}
