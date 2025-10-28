package com.carpool.demo.controller;

import com.carpool.demo.model.user.User;
import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserManager userManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userManager.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String mobile = loginData.get("mobileNumber");
        String password = loginData.get("password");

        User user = userRepository.findByMobileNumber(mobile);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not found"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid password"));
        }
        // JWT erzeugen
        String token = jwtUtils.generateToken(user.getName(), user.getUserid());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "userId", user.getUserid(),
                "name", user.getName()
        ));
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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
                "message", "Logout successful — please delete your token on the client side"
        ));
    }


}
