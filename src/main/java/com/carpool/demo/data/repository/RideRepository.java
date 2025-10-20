package com.carpool.demo.data.repository;
import com.carpool.demo.model.ride.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {

    // Suche nach Start- und Zielort (Groß-/Kleinschreibung egal)
    List<Ride> findByStartLocationContainingIgnoreCaseAndDestinationContainingIgnoreCase(
            String startLocation,
            String destination
    );

    // Optional: Suche nur nach Startort
    List<Ride> findByStartLocationContainingIgnoreCase(String startLocation);

    // Optional: Suche nur nach Zielort
    List<Ride> findByDestinationContainingIgnoreCase(String destination);
}

