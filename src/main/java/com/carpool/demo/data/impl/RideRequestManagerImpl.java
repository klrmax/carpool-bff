package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRequestRepository;
import com.carpool.demo.model.ride.RideRequest;
import com.carpool.demo.model.ride.RequestStatus;
import com.carpool.demo.model.user.User;
import com.carpool.demo.model.ride.Ride;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideRequestManagerImpl implements RideRequestManager {

    private final RideRequestRepository repo;

    public RideRequestManagerImpl(RideRequestRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public RideRequest createRequest(User passenger, Ride ride, String message) {
        RideRequest req = new RideRequest();
        req.setPassenger(passenger);
        req.setRide(ride);
        req.setMessage(message);
        req.setStatus(RequestStatus.PENDING);
        return repo.save(req);
    }

    @Override
    @Transactional
    public RideRequest changeStatus(Long requestId, RequestStatus newStatus) {
        RideRequest req = repo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden"));
        req.setStatus(newStatus);
        return repo.save(req);
    }

    @Override
    public List<RideRequest> getOpenRequestsForDriver(User driver) {
        return repo.findByRideDriverAndStatus(driver, RequestStatus.PENDING);
    }

    @Override
    public List<RideRequest> getRequestsForPassenger(User passenger) {
        return repo.findByPassenger(passenger);
    }
}
