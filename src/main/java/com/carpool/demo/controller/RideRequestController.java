package com.carpool.demo.controller;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.*;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.AuthUtils;
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
    private final AuthUtils authUtils;

    public RideRequestController(RideRequestManager manager, RideRepository rideRepo,
                                 UserRepository userRepo, AuthUtils authUtils) {
        this.manager = manager;
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.authUtils = authUtils;
    }

    // ----------------------------
    // 1Anfrage erstellen (Mitfahrer klickt „Mitfahren“)
    // ----------------------------
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestParam Integer rideId,
            @RequestBody(required = false) String message,
            @RequestHeader("Authorization") String token) {

        try {
            // Nutzer anhand Token ermitteln
            User passenger = authUtils.getUserFromToken(token);

            // Fahrt prüfen
            Optional<Ride> rideOpt = rideRepo.findById(rideId);
            if (rideOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Fahrt mit ID " + rideId + " wurde nicht gefunden"));
            }

            Ride ride = rideOpt.get();

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------
    // Offene Anfragen für Fahrer
    // ----------------------------
    @GetMapping("/open")
    public ResponseEntity<?> getOpenRequests(@RequestHeader("Authorization") String token) {
        try {
            User driver = authUtils.getUserFromToken(token);
            List<RideRequest> openRequests = manager.getOpenRequestsForDriver(driver);
            return ResponseEntity.ok(openRequests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
            @RequestHeader("Authorization") String token) {

        try {
            // Hier könntest du optional prüfen, ob Token zum Fahrer gehört
            authUtils.getUserFromToken(token);

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
    public ResponseEntity<?> getPassengerRequests(@RequestHeader("Authorization") String token) {
        try {
            User passenger = authUtils.getUserFromToken(token);
            List<RideRequest> requests = manager.getRequestsForPassenger(passenger);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
