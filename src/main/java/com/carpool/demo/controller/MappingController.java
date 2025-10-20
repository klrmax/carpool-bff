package com.carpool.demo.controller;
import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.data.impl.PropertyFileUserManagerImpl;
import com.carpool.demo.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class MappingController {

    @GetMapping("/test")
    public String testEndpoint() {
        return "Backend läuft korrekt!";
    }

    private final UserManager userManager = new PropertyFileUserManagerImpl();

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        boolean success = userManager.registerUser(
                user.getMobileNumber(),
                user.getPassword(),
                user.getName()

        );

        if (!success) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "E-Mail existiert bereits!"));
        }

        return ResponseEntity
                .ok(Map.of("message", "Registrierung erfolgreich!"));
    }

    @PostMapping("/login")
    public String login(@RequestParam String mobileNumber, @RequestParam String password) {
        User user = userManager.login(mobileNumber, password);
        if (user == null) return "Login fehlgeschlagen!";
        return "Login erfolgreich! Token: " + user.getToken();
    }

    @PostMapping("/logout")
    public String logout(@RequestParam String mobileNumber) {
        boolean success = userManager.logout(mobileNumber);
        return success ? "Logout erfolgreich!" : "Logout fehlgeschlagen!";
    }
    @GetMapping("/")
    public String root() {
        return "Willkommen bei der Carpool API!";
    }
}


