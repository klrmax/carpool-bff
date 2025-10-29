package com.carpool.demo.controller;

import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.RideRequestRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.utils.AuthUtils;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    @Autowired
    private RideManager rideManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRequestRepository  rideRequestRepository;

    @Autowired
    private AuthUtils authUtils;

    // Alle Fahrten abrufen
    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = rideManager.getAllRides();
        return ResponseEntity.ok(rides);
    }

    // Suche nach Start, Ziel, Datum und Uhrzeit
    @GetMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date,   // z. B. "2025-12-25"
            @RequestParam(required = false) String time) { // z. B. "16:30"

        List<Ride> rides = rideManager.searchRides(start, destination, date, time);
        return ResponseEntity.ok(rides);
    }

    // Neue Fahrt erstellen
    @PostMapping
    public ResponseEntity<?> createRide(
            @RequestBody Ride ride,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authUtils.extractToken(authHeader);
            int userId = jwtUtils.extractUserId(token);

            Ride newRide = rideManager.createRide(ride, userId);
            return ResponseEntity.ok(newRide);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültiger oder abgelaufener Token"));
        }
    }


    // Einzelne Fahrt per ID abrufen
    @GetMapping("/{id}")
    public ResponseEntity<Ride> getRideById(@PathVariable Integer id) {
        Ride ride = rideManager.getRideById(id);
        if (ride == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMyRides(@RequestHeader("Authorization") String authHeader) {

        try {
            String token = authUtils.extractToken(authHeader);

            // User-ID aus Token extrahieren
            int userId = jwtUtils.extractUserId(token);

            //Fahrten finden, bei denen dieser User der Fahrer ist
            List<Ride> rides = rideRepository.findByDriverUserid(userId);

            return ResponseEntity.ok(rides);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültiger oder abgelaufener Token"));
        }
    }

    @GetMapping("/joined")
    public ResponseEntity<?> getJoinedRides(@RequestHeader("Authorization") String authHeader) {
        try {

            String token = authUtils.extractToken(authHeader);

            // User-ID aus Token extrahieren
            int userId = jwtUtils.extractUserId(token);

            // Fahrten finden, bei denen der User Mitfahrer ist
            List<Ride> joinedRides = rideRequestRepository.findAcceptedRidesByPassengerId(userId);

            return ResponseEntity.ok(joinedRides);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Ungültiger oder abgelaufener Token"));
        }
    }
    // Fahrt löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable Integer id) {
        rideManager.deleteRide(id);
        return ResponseEntity.ok().build();
    }

}
