package com.carpool.demo.controller;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.*;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.AuthUtils;
import com.carpool.demo.utils.JwtUtils;
import com.carpool.demo.utils.UserCache;
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
    private final AuthUtils authUtils;
    private final UserCache userCache;

    public RideRequestController(RideRequestManager manager,
                                 RideRepository rideRepo,
                                 UserRepository userRepo,
                                 JwtUtils jwtUtils,
                                 AuthUtils authUtils,
                                 UserCache userCache) {
        this.manager = manager;
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
        this.jwtUtils = jwtUtils;
        this.authUtils = authUtils;
        this.userCache = userCache;
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
            String token = authUtils.extractToken(authHeader);

            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger oder abgelaufener Token"));
            }

            Integer passengerId = jwtUtils.extractUserId(token);
            User passenger = userCache.getUserById(passengerId);

            Ride ride = rideRepo.findById(rideId)
                    .orElseThrow(() -> new IllegalArgumentException("Fahrt mit ID " + rideId + " wurde nicht gefunden"));

            //Null-Check für Driver
            if (ride.getDriver() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Fahrt hat keinen Fahrer zugewiesen"));
            }

            // Fahrer darf sich nicht selbst als Mitfahrer hinzufügen
            if (ride.getDriver().getUserid() == passenger.getUserid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Du kannst nicht als Mitfahrer bei deiner eigenen Fahrt beitreten"));
            }

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
            String token = authUtils.extractToken(authHeader);

            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger Token"));
            }

            Integer driverId = jwtUtils.extractUserId(token);
            User driver = userCache.getUserById(driverId);

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
            String token = authUtils.extractToken(authHeader);
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
            String token = authUtils.extractToken(authHeader);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültiger Token"));
            }

            Integer passengerId = jwtUtils.extractUserId(token);
            User passenger = userCache.getUserById(passengerId);
            List<RideRequest> requests = manager.getRequestsForPassenger(passenger);

            // 🔧 Mappe in ein sauberes JSON-Response-Format
            List<Map<String, Object>> result = requests.stream().map(req -> {
                Map<String, Object> rideData = new HashMap<>();
                Ride ride = req.getRide();

                // Fahrtdaten
                rideData.put("rideId", ride.getId());
                rideData.put("start", ride.getStartLocation());
                rideData.put("destination", ride.getDestination());
                rideData.put("departureTime", ride.getDepartureTime());
                rideData.put("availableSeats", ride.getAvailableSeats());

                // Fahrerinfo
                Map<String, Object> driverInfo = new HashMap<>();
                driverInfo.put("driverId", ride.getDriver().getUserid());
                driverInfo.put("driverName", ride.getDriver().getName());
                rideData.put("driver", driverInfo);

                // Anfrage-Infos
                rideData.put("requestId", req.getId());
                rideData.put("status", req.getStatus());
                rideData.put("message", req.getMessage());
                rideData.put("createdAt", req.getCreatedAt());

                return rideData;
            }).toList();

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // ----------------------------
    // Hilfsmethode zum Token-Handling
    // ----------------------------


}
