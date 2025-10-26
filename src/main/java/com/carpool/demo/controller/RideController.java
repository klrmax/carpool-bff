package com.carpool.demo.controller;

import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    @Autowired
    private RideManager rideManager;

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
    public ResponseEntity<Ride> createRide(
            @RequestBody Ride ride,
            @RequestParam(required = true) int userid) {

        Ride newRide = rideManager.createRide(ride, userid);
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
        return ResponseEntity.ok().build();
    }
}
