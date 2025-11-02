package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.RideRequestManager;
import com.carpool.demo.data.repository.RideRepository;
import com.carpool.demo.data.repository.RideRequestRepository;
import com.carpool.demo.model.ride.Ride;
import com.carpool.demo.model.ride.RideRequest;
import com.carpool.demo.model.ride.RequestStatus;
import com.carpool.demo.model.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RideRequestManagerImpl implements RideRequestManager {

    private final RideRequestRepository requestRepo;
    private final RideRepository rideRepo;

    public RideRequestManagerImpl(RideRequestRepository requestRepo, RideRepository rideRepo) {
        this.requestRepo = requestRepo;
        this.rideRepo = rideRepo;
    }

    @Override
    @Transactional
    public RideRequest createRequest(User passenger, Ride ride, String message) {
        RideRequest req = new RideRequest();
        req.setPassenger(passenger);
        req.setRide(ride);
        req.setMessage(message);
        req.setStatus(RequestStatus.PENDING);
        return requestRepo.save(req);
    }

    @Override
    @Transactional
    public RideRequest changeStatus(Long requestId, RequestStatus newStatus) {
        RideRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Anfrage nicht gefunden"));

        Ride ride = req.getRide();

        // Wenn Anfrage auf ACCEPTED gesetzt wird → Sitz reduzieren
        if (newStatus == RequestStatus.ACCEPTED) {
            if (ride.getAvailableSeats() <= 0) {
                throw new IllegalStateException("Keine verfügbaren Sitzplätze mehr in dieser Fahrt.");
            }


            ride.setAvailableSeats(ride.getAvailableSeats() - 1);
            rideRepo.save(ride);
        }

        req.setStatus(newStatus);
        return requestRepo.save(req);
    }

    @Override
    public List<RideRequest> getOpenRequestsForDriver(User driver) {
        return requestRepo.findByRideDriverAndStatus(driver, RequestStatus.PENDING);
    }

    @Override
    public List<RideRequest> getRequestsForPassenger(User passenger) {
        return requestRepo.findByPassenger(passenger);
    }

    @Override
    public boolean existsByRideAndPassenger(Ride ride, User passenger) {
        return requestRepo.existsByRideAndPassenger(ride, passenger);
    }
}
