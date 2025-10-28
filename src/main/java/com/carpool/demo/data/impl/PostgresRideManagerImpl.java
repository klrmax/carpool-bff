package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import java.util.List;

@Service
public class PostgresRideManagerImpl implements RideManager {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserManager userManager;

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public List<Ride> searchRides(String start, String destination, String date, String time) {
        LocalDateTime from = null;
        LocalDateTime to = null;

        try {
            if (date != null && !date.isEmpty()) {
                // Wenn auch Uhrzeit angegeben ist, such ab dieser Uhrzeit ± 2 Stunden
                if (time != null && !time.isEmpty()) {
                    from = LocalDateTime.parse(date + "T" + time + ":00");
                    to = from.plusHours(2);
                } else {
                    // Nur Datum angegeben → ganzer Tag
                    from = LocalDateTime.parse(date + "T00:00:00");
                    to = from.plusDays(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Fehler beim Parsen von Datum/Zeit: " + e.getMessage());
        }

        // Keine Zeitangabe → einfache Start/Ziel-Suche
        if (from == null) {
            if (start != null && destination != null) {
                return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCase(start, destination);
            } else if (start != null) {
                return rideRepository.findByStartLocationContainingIgnoreCase(start);
            } else if (destination != null) {
                return rideRepository.findByDestinationContainingIgnoreCase(destination);
            } else {
                return rideRepository.findAll();
            }
        }

        // Suche nach Start, Ziel und Zeitbereich
        return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCaseAndDepartureTimeBetween(
                start, destination, from, to
        );
    }

    @Override
    public Ride createRide(Ride ride, int userid) {
        // Fahrer automatisch aus userid ermitteln
        User driver = userManager.getUserById(userid);
        ride.setDriver(driver);

        // Zeitstempel setzen
        ride.setCreatedAt(LocalDateTime.now());


        return rideRepository.save(ride);
    }

    @Override
    public Ride getRideById(Integer id) {
        return rideRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteRide(Integer id) {
        rideRepository.deleteById(id);
    }
}
