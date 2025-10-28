package com.carpool.demo.data.api;

import com.carpool.demo.model.ride.RideRequest;
import com.carpool.demo.model.ride.RequestStatus;
import com.carpool.demo.model.user.User;
import com.carpool.demo.model.ride.Ride;

import java.util.List;

public interface RideRequestManager {
    RideRequest createRequest(User passenger, Ride ride, String message);
    RideRequest changeStatus(Long requestId, RequestStatus newStatus);
    List<RideRequest> getOpenRequestsForDriver(User driver);
    List<RideRequest> getRequestsForPassenger(User passenger);
    boolean existsByRideAndPassenger(Ride ride, User passenger);

}
