package com.carpool.demo.graphql;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.model.user.User;
import com.carpool.demo.model.user.AuthPayload;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserGraphQLController {

    private final UserManager userManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public UserGraphQLController(UserManager userManager, JwtUtils jwtUtils) {
        this.userManager = userManager;
        this.jwtUtils = jwtUtils;
    }

    @MutationMapping
    public AuthPayload register(
            @Argument String name,
            @Argument String mobileNumber,
            @Argument String password
    ) {
        // Benutzer registrieren
        User newUser = new User();
        newUser.setName(name);
        newUser.setMobileNumber(mobileNumber);
        newUser.setPassword(password);
        User savedUser = userManager.registerUser(newUser);

        // Token generieren
        String token = jwtUtils.generateToken(savedUser.getName(), savedUser.getUserid());

        return new AuthPayload(token, savedUser);
    }

    @MutationMapping
    public AuthPayload login(
            @Argument String mobileNumber,
            @Argument String password
    ) {
        // Benutzer verifizieren
        User user = userManager.loginUser(mobileNumber, password);

        // Token erzeugen
        String token = jwtUtils.generateToken(user.getName(), user.getUserid());

        return new AuthPayload(token, user);
    }
}
