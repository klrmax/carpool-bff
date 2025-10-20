package com.carpool.demo.data.api;

import com.carpool.demo.model.ride.Ride;

import java.util.List;

public interface RideManager {
    List<Ride> getAllRides();
    List<Ride> searchRides(String start, String destination);
    Ride createRide(Ride ride);
    Ride getRideById(Integer id);
    void deleteRide(Integer id);
}
