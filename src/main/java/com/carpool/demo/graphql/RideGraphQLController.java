package com.carpool.demo.graphql;

import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.AuthUtils;
import graphql.GraphQLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RideGraphQLController {

    private final RideManager rideManager;
    private final AuthUtils authUtils;

    @Autowired
    public RideGraphQLController(RideManager rideManager, AuthUtils authUtils) {
        this.rideManager = rideManager;
        this.authUtils = authUtils;
    }

    // 🔹 Mutation: Fahrt erstellen
    @MutationMapping
    public Ride createRide(
            @Argument String startLocation,
            @Argument String destination,
            @Argument String departureTime,
            @Argument Integer availableSeats,
            GraphQLContext context
    ) {
        String token = context.get("Authorization");
        User user = authUtils.getUserFromToken(token);

        Ride ride = new Ride();
        ride.setStartLocation(startLocation);
        ride.setDestination(destination);

        try {
            ride.setDepartureTime(LocalDateTime.parse(departureTime));
        } catch (Exception e) {
            System.err.println("⚠️ Fehler beim Parsen von departureTime: " + departureTime);
            throw new RuntimeException("Ungültiges Datumsformat. Erwartet: yyyy-MM-dd'T'HH:mm:ss");
        }

        ride.setAvailableSeats(availableSeats);

        System.out.println("🕒 departureTime vorm Speichern: " + ride.getDepartureTime());

        return rideManager.createRide(ride, user.getUserid());
    }

    // 🔹 Query: Alle Fahrten abrufen
    @QueryMapping
    public List<Ride> getAllRides() {
        return rideManager.getAllRides();
    }

    // 🔹 Query: Fahrt per ID abrufen
    @QueryMapping
    public Ride getRideById(@Argument Integer id) {
        return rideManager.getRideById(id);
    }

    // 🔹 Query: Fahrten suchen
    @QueryMapping
    public List<Ride> searchRides(
            @Argument String start,
            @Argument String destination,
            @Argument String date,
            @Argument String time
    ) {
        return rideManager.searchRides(start, destination, date, time);
    }
}
