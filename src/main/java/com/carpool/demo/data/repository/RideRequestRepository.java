package com.carpool.demo.data.repository;

import com.carpool.demo.model.ride.RideRequest;
import com.carpool.demo.model.ride.RequestStatus;
import com.carpool.demo.model.user.User;
import com.carpool.demo.model.ride.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {
    List<RideRequest> findByRideDriverAndStatus(User driver, RequestStatus status); // offene für Fahrer
    List<RideRequest> findByPassenger(User passenger); // eigene Anfragen
    List<RideRequest> findByRide(Ride ride); // alle zu einer Fahrt
    boolean existsByRideAndPassenger(Ride ride, User passenger);
    @Query("SELECT rr.ride FROM RideRequest rr " +
            "WHERE rr.passenger.userid = :userId AND rr.status = 'ACCEPTED'")
    List<Ride> findAcceptedRidesByPassengerId(@Param("userId") int userId);

}