package com.carpool.demo.controller;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.*;
import com.carpool.demo.model.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideRequestController {

    private final RideRequestManager manager;
    private final RideRepository rideRepo;
    private final UserRepository userRepo;

    public RideRequestController(RideRequestManager manager,
                                 RideRepository rideRepo,
                                 UserRepository userRepo) {
        this.manager = manager;
        this.rideRepo = rideRepo;
        this.userRepo = userRepo;
    }

    // Mitfahrer stellt Anfrage
    @PostMapping("/{rideId}/request")
    public ResponseEntity<RideRequest> requestRide(
            @PathVariable Integer rideId,
            @RequestParam Integer passengerId,
            @RequestBody(required = false) String message) {

        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrt nicht gefunden"));
        User passenger = userRepo.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden"));

        RideRequest req = manager.createRequest(passenger, ride, message);
        return ResponseEntity.ok(req);
    }

    // Fahrer sieht offene Anfragen
    @GetMapping("/requests/open")
    public ResponseEntity<List<RideRequest>> openRequests(@RequestParam Integer driverId) {
        User driver = userRepo.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Fahrer nicht gefunden"));
        return ResponseEntity.ok(manager.getOpenRequestsForDriver(driver));
    }

    // Fahrer nimmt an / lehnt ab
    @PatchMapping("/requests/{id}/status")
    public ResponseEntity<RideRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status) {
        RideRequest req = manager.changeStatus(id, status);
        return ResponseEntity.ok(req);
    }

    // Mitfahrer sieht eigene Anfragen
    @GetMapping("/requests/mine")
    public ResponseEntity<List<RideRequest>> myRequests(@RequestParam Integer passengerId) {
        User passenger = userRepo.findById(passengerId)
                .orElseThrow(() -> new IllegalArgumentException("Nutzer nicht gefunden"));
        return ResponseEntity.ok(manager.getRequestsForPassenger(passenger));
    }
}
