package com.carpool.demo.graphql;

import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.exception.GraphQLRequestException;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.AuthUtils;
import graphql.GraphQLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
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

    // Mutation: Fahrt erstellen (GESCHÜTZT - Token erforderlich!)
    @MutationMapping
    public Ride createRide(
            @Argument String startLocation,
            @Argument String destination,
            @Argument String departureTime,
            @Argument Integer availableSeats,
            GraphQLContext context
    ) {
        // Token validieren (IMMER erforderlich für createRide!)
        String token = context.get("Authorization");
        User user = authUtils.getUserFromToken(token);
        if (user == null) {
            throw new GraphQLRequestException("Nicht autorisiert: Ungültiges oder fehlendes Token", ErrorType.UNAUTHORIZED);
        }

        // Eingaben prüfen
        if (startLocation == null || startLocation.isBlank()) {
            throw new GraphQLRequestException("Startort darf nicht leer sein", ErrorType.BAD_REQUEST);
        }
        if (destination == null || destination.isBlank()) {
            throw new GraphQLRequestException("Zielort darf nicht leer sein", ErrorType.BAD_REQUEST);
        }
        if (availableSeats == null || availableSeats <= 0) {
            throw new GraphQLRequestException("Die Anzahl verfügbarer Sitzplätze muss größer als 0 sein", ErrorType.BAD_REQUEST);
        }

        // Abfahrtszeit prüfen
        LocalDateTime parsedDepartureTime;
        try {
            parsedDepartureTime = LocalDateTime.parse(departureTime);
        } catch (Exception e) {
            throw new GraphQLRequestException(
                    "Ungültiges Datumsformat für 'departureTime'. Erwartet: yyyy-MM-dd'T'HH:mm:ss",
                    ErrorType.BAD_REQUEST
            );
        }

        // Fahrt-Objekt aufbauen
        Ride ride = new Ride();
        ride.setStartLocation(startLocation.trim());
        ride.setDestination(destination.trim());
        ride.setDepartureTime(parsedDepartureTime);
        ride.setAvailableSeats(availableSeats);

        System.out.println("Abfahrtszeit vor dem Speichern: " + ride.getDepartureTime());

        // Fahrt speichern
        try {
            return rideManager.createRide(ride, user.getUserid());
        } catch (Exception e) {
            throw new GraphQLRequestException("Fehler beim Erstellen der Fahrt: " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }
    }

    // Query: Alle Fahrten abrufen (ÖFFENTLICH - kein Token nötig!)
    @QueryMapping
    public List<Ride> getAllRides(GraphQLContext context) {
        // Öffentlich - kein Token erforderlich (wie REST /api/ride)
        return rideManager.getAllRides();
    }

    // Query: Fahrt per ID abrufen (ÖFFENTLICH - kein Token nötig!)
    @QueryMapping
    public Ride getRideById(@Argument Integer id, GraphQLContext context) {
        // Öffentlich - kein Token erforderlich
        return rideManager.getRideById(id);
    }

    // Query: Fahrten suchen (ÖFFENTLICH - kein Token nötig!)
    @QueryMapping
    public List<Ride> searchRides(
            @Argument String start,
            @Argument String destination,
            @Argument String date,
            @Argument String time,
            GraphQLContext context
    ) {
        // Öffentlich - kein Token erforderlich (wie REST /api/ride/search)
        
        if ((start == null || start.isBlank()) && (destination == null || destination.isBlank())) {
            throw new GraphQLRequestException("Bitte Start- oder Zielort angeben", ErrorType.BAD_REQUEST);
        }

        List<Ride> rides;
        try {
            rides = rideManager.searchRides(start, destination, date, time);
        } catch (Exception e) {
            throw new GraphQLRequestException("Fehler bei der Fahrtsuche: " + e.getMessage(), ErrorType.INTERNAL_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();

        // Filter: Nur Fahrten mit freien Plätzen UND in der Zukunft
        List<Ride> filtered = rides.stream()
                .filter(ride -> ride.getAvailableSeats() > 0)
                .filter(ride -> ride.getDepartureTime() != null && ride.getDepartureTime().isAfter(now))
                .toList();

        if (filtered.isEmpty()) {
            throw new GraphQLRequestException("Keine passenden Fahrten mit freien Sitzplätzen gefunden", ErrorType.NOT_FOUND);
        }

        return filtered;
    }
}
