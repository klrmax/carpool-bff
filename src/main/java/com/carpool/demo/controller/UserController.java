package com.carpool.demo.controller;

import com.carpool.demo.model.user.User;
import com.carpool.demo.data.api.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserManager userManager;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userManager.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            User user = userManager.loginUser(
                    loginData.get("mobileNumber"),
                    loginData.get("password")
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", user.getToken(),
                    "expiresAt", user.getTokenExpiration(),
                    "userId", user.getUserid()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        return userManager.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userManager.deleteUser(id);
    }
}
