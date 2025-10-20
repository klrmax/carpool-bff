package com.carpool.demo.controller;

import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RideManager rideManager;

    // Alle Fahrten abrufen
    @GetMapping
    public ResponseEntity<List<Ride>> getAllRides() {
        List<Ride> rides = rideManager.getAllRides();
        return ResponseEntity.ok(rides);
    }

    // Suche nach Start & Ziel
    @GetMapping("/search")
    public ResponseEntity<List<Ride>> searchRides(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String destination) {

        List<Ride> rides = rideManager.searchRides(start, destination);
        return ResponseEntity.ok(rides);
    }

    // Neue Fahrt erstellen
    @PostMapping
    public ResponseEntity<Ride> createRide(@RequestBody Ride ride) {
        Ride newRide = rideManager.createRide(ride);
        return ResponseEntity.ok(newRide);
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

    // Fahrt löschen
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable Integer id) {
        rideManager.deleteRide(id);
        return ResponseEntity.noContent().build();
    }
}
