package com.carpool.demo.graphql;

import com.carpool.demo.data.api.RideManager;
import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.exception.GraphQLRequestException;
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
import org.springframework.graphql.execution.ErrorType;
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
            throw new GraphQLRequestException("Fahrt wurde nicht gefunden", ErrorType.NOT_FOUND);
        }

        // int-Vergleich mit ==
        if (ride.getDriver() != null && ride.getDriver().getUserid() == passenger.getUserid()) {
            throw new GraphQLRequestException("Du kannst keine Anfrage für deine eigene Fahrt stellen", ErrorType.BAD_REQUEST);
        }

        if (rideRequestManager.existsByRideAndPassenger(ride, passenger)) {
            throw new GraphQLRequestException("Du hast bereits eine Anfrage für diese Fahrt gestellt", ErrorType.BAD_REQUEST);
        }

        return rideRequestManager.createRequest(passenger, ride, message);
    }

    // Query: Eigene Mitfahranfragen (als Passenger)
    @QueryMapping
    public List<RideRequest> getRideRequests(GraphQLContext context) {
        String token = context.get("Authorization");
        if (token == null || token.isBlank()) {
            throw new GraphQLRequestException("Nicht autorisiert: Kein Token übergeben", ErrorType.UNAUTHORIZED);
        }
        User user = authUtils.getUserFromToken(token);
        return rideRequestManager.getRequestsForPassenger(user);
    }

    // Query: Offene Anfragen für eigene Fahrten (als Driver)
    @QueryMapping
    public List<RideRequest> getOpenRideRequests(GraphQLContext context) {
        String token = context.get("Authorization");
        if (token == null || token.isBlank()) {
            throw new GraphQLRequestException("Nicht autorisiert: Kein Token übergeben", ErrorType.UNAUTHORIZED);
        }
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
            throw new GraphQLRequestException(
                    "Ungültiger Status: " + status + " (erlaubt: PENDING, ACCEPTED, REJECTED)",
                    ErrorType.BAD_REQUEST
            );
        }

        RideRequest request = rideRequestManager.changeStatus(requestId, newStatus);

        // int-Vergleich mit ==
        if (request.getRide().getDriver() == null ||
                request.getRide().getDriver().getUserid() != driver.getUserid()) {
            throw new GraphQLRequestException("Nur der Fahrer darf den Status dieser Anfrage ändern", ErrorType.UNAUTHORIZED);
        }

        return request;
    }
}
