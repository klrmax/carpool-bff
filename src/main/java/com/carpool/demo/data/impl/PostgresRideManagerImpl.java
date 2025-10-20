package com.carpool.demo.data.impl;

import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.data.api.RideManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostgresRideManagerImpl implements RideManager {

    @Autowired
    private RideRepository rideRepository;

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public List<Ride> searchRides(String start, String destination) {
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

    @Override
    public Ride createRide(Ride ride) {
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
