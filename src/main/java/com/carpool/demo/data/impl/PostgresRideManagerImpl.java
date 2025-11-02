package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.UserManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.model.user.User;
import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


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
    @Cacheable("rides_all")
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public List<Ride> searchRides(String start, String destination, String date, String time) {
        LocalDateTime from = null;
        LocalDateTime to = null;

        // Eingaben normalisieren – leere Strings als null behandeln
        start = (start != null && !start.trim().isEmpty()) ? start.trim() : null;
        destination = (destination != null && !destination.trim().isEmpty()) ? destination.trim() : null;
        date = (date != null && !date.trim().isEmpty()) ? date.trim() : null;
        time = (time != null && !time.trim().isEmpty()) ? time.trim() : null;

        // Wenn gar nichts übergeben wurde → leere Liste zurückgeben
        if (start == null && destination == null && date == null) {
            System.out.println("Keine Suchparameter angegeben – keine Ergebnisse.");
            return List.of();
        }

        // Datum/Zeit parsen, falls angegeben
        if (date != null) {
            try {
                if (time != null) {
                    from = LocalDateTime.parse(date + "T" + time + ":00");
                    to = from.plusHours(2);
                } else {
                    from = LocalDateTime.parse(date + "T00:00:00");
                    to = from.plusDays(1);
                }
            } catch (Exception e) {
                System.out.println("Fehler beim Parsen von Datum/Zeit: " + e.getMessage());
            }
        }

        // Falls kein Datum angegeben → Suche nur nach Start/Ziel
        if (from == null) {
            if (start != null && destination != null) {
                return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCase(start, destination);
            } else if (start != null) {
                return rideRepository.findByStartLocationContainingIgnoreCase(start);
            } else if (destination != null) {
                return rideRepository.findByDestinationContainingIgnoreCase(destination);
            }
        }

        // Falls Datum vorhanden → Suche nach Ort + Zeitbereich
        return rideRepository.findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCaseAndDepartureTimeBetween(
                start != null ? start : "",
                destination != null ? destination : "",
                from,
                to
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

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Map<String, Object>> searchParallel(String from, String to, String date) {
        try {
            // Starte Carpool-Suche parallel
            CompletableFuture<List<Ride>> carpoolFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return searchRides(from, to, date, null);  // ← Nutze bestehende Methode
                } catch (Exception e) {
                    System.out.println("Fehler bei Carpool-Suche: " + e.getMessage());
                    return new ArrayList<>();
                }
            });

            // Starte DB-API-Suche parallel
            CompletableFuture<String> trainsFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    DBTrainManagerImpl trainManager = new DBTrainManagerImpl();
                    return trainManager.getTrainData(from, to, date, "08");  // ← Korrekte Methode mit hour="08"
                } catch (Exception e) {
                    System.out.println("Fehler bei Bahn-Suche: " + e.getMessage());
                    return "{}";
                }
            });

            // Warte bis beide fertig sind
            CompletableFuture.allOf(carpoolFuture, trainsFuture).join();

            // Kombiniere Ergebnisse
            Map<String, Object> result = new HashMap<>();
            result.put("carpoolRides", carpoolFuture.join());
            result.put("trainData", trainsFuture.join());  // ← Jetzt als String
            result.put("timestamp", System.currentTimeMillis());

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            System.out.println("Fehler bei paralleler Suche: " + e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }



}
