package com.carpool.demo.utils;

import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.user.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class UserCache {

    private final Map<Integer, User> cache = new HashMap<>();
    private final UserRepository userRepository;

    public UserCache(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(int id) {
        // Wenn der User schon im Cache ist -> kein DB-Zugriff nötig
        if (cache.containsKey(id)) {
            System.out.println(" Loaded user " + id + " from cache");
            return cache.get(id);
        }

        // Wenn nicht -> DB-Abfrage, speichern und zurückgeben
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(value -> cache.put(id, value));
        return user.orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void clear() {
        cache.clear();
    }
}
