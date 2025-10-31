package com.carpool.demo.graphql;

import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.model.ride.RideRequest;
import com.carpool.demo.model.ride.RequestStatus;
import com.carpool.demo.model.user.User;
import com.carpool.demo.utils.AuthUtils;
import graphql.GraphQLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RideRequestGraphQLController {

    private final RideRequestManager rideRequestManager;
    private final RideManager rideManager;
    private final AuthUtils authUtils;

    @Autowired
    public RideRequestGraphQLController(
            RideRequestManager rideRequestManager,
            RideManager rideManager,
            AuthUtils authUtils
    ) {
        this.rideRequestManager = rideRequestManager;
        this.rideManager = rideManager;
        this.authUtils = authUtils;
    }

    // Mutation: Mitfahranfrage erstellen
    @MutationMapping
    public RideRequest createRideRequest(
            @Argument Integer rideId,
            @Argument String message,
            GraphQLContext context
    ) {
        String token = context.get("Authorization");
        User passenger = authUtils.getUserFromToken(token);

        Ride ride = rideManager.getRideById(rideId);
        if (ride == null) {
            throw new RuntimeException("Fahrt nicht gefunden");
        }

        if (ride.getDriver() != null && ride.getDriver().getUserid() == passenger.getUserid()) {
            throw new RuntimeException("Du kannst nicht bei deiner eigenen Fahrt mitfahren");
        }

        if (rideRequestManager.existsByRideAndPassenger(ride, passenger)) {
            throw new RuntimeException("Du hast für diese Fahrt bereits eine Anfrage gestellt");
        }

        return rideRequestManager.createRequest(passenger, ride, message);
    }

    //Query: Eigene Mitfahranfragen (als Passenger)
    @QueryMapping
    public List<RideRequest> getRideRequests(GraphQLContext context) {
        String token = context.get("Authorization");
        User user = authUtils.getUserFromToken(token);
        return rideRequestManager.getRequestsForPassenger(user);
    }

    // Query: Offene Anfragen für eigene Fahrten (als Driver)
    @QueryMapping
    public List<RideRequest> getOpenRideRequests(GraphQLContext context) {
        String token = context.get("Authorization");
        User user = authUtils.getUserFromToken(token);
        return rideRequestManager.getOpenRequestsForDriver(user);
    }

    // Mutation: Status einer Mitfahranfrage ändern (nur Fahrer)
    @MutationMapping
    public RideRequest changeRideRequestStatus(
            @Argument Long requestId,
            @Argument String status,
            GraphQLContext context
    ) {
        String token = context.get("Authorization");
        User driver = authUtils.getUserFromToken(token);

        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Ungültiger Status: " + status + " (erlaubt: PENDING, ACCEPTED, REJECTED)");
        }

        RideRequest request = rideRequestManager.changeStatus(requestId, newStatus);
        if (request.getRide().getDriver() == null ||
                request.getRide().getDriver().getUserid() != driver.getUserid()) {
            throw new RuntimeException("Nur der Fahrer dieser Fahrt darf den Status ändern");
        }

        return request;
    }
}
