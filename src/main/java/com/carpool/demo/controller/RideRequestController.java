package com.carpool.demo.controller;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.*;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ride-request")
public class RideRequestController {

    private final RideRequestManager manager;
    private final RideRepository rideRepo;
    private final UserRepository userRepo;
    private final JwtUtils jwtUtils;

    public RideRequestController(RideRequestManager manager,
                                 RideRepository rideRepo,
                                 UserRepository userRepo,
                                 JwtUtils jwtUtils) {
        this.manager = manager;
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
    }

    // ----------------------------
    // Anfrage erstellen (Mitfahrer klickt „Mitfahren“)
    // ----------------------------
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestParam Integer rideId,
            @RequestBody(required = false) String message,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Token aus Header extrahieren
            String token = extractToken(authHeader);

            // Token validieren & User auslesen
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger oder abgelaufener Token"));
            }

            Integer passengerId = jwtUtils.extractUserId(token);
            User passenger = userRepo.findById(passengerId)
                    .orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden"));

            // Fahrt prüfen
            Ride ride = rideRepo.findById(rideId)
                    .orElseThrow(() -> new IllegalArgumentException("Fahrt mit ID " + rideId + " wurde nicht gefunden"));

            // Doppelte Anfrage prüfen
            boolean alreadyExists = manager.existsByRideAndPassenger(ride, passenger);
            if (alreadyExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Du hast für diese Fahrt bereits eine Anfrage gestellt"));
            }

            // Anfrage erstellen
            RideRequest request = manager.createRequest(passenger, ride, message);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------
    // Offene Anfragen für Fahrer
    // ----------------------------
    @GetMapping("/open")
    public ResponseEntity<?> getOpenRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);

            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger Token"));
            }

            Integer driverId = jwtUtils.extractUserId(token);
            User driver = userRepo.findById(driverId)
                    .orElseThrow(() -> new IllegalArgumentException("Fahrer nicht gefunden"));

            List<RideRequest> openRequests = manager.getOpenRequestsForDriver(driver);
            return ResponseEntity.ok(openRequests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------
    // Fahrer nimmt an oder lehnt ab
    // ----------------------------
    @PatchMapping("/{requestId}")
    public ResponseEntity<?> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestParam RequestStatus status,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger Token"));
            }

            RideRequest updated = manager.changeStatus(requestId, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------
    // Mitfahrer sieht eigene Anfragen
    // ----------------------------
    @GetMapping("/mine")
    public ResponseEntity<?> getPassengerRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger Token"));
            }

            Integer passengerId = jwtUtils.extractUserId(token);
            User passenger = userRepo.findById(passengerId)
                    .orElseThrow(() -> new IllegalArgumentException("Mitfahrer nicht gefunden"));

            List<RideRequest> requests = manager.getRequestsForPassenger(passenger);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------
    // Hilfsmethode zum Token-Handling
    // ----------------------------
    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Kein gültiger Authorization-Header");
        }
        return authHeader.substring(7);
    }
}
