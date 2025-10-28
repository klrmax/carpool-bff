package com.carpool.demo.model.ride;

import com.carpool.demo.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_requests")
public class RideRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mitfahrer (der anfragt)
    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // Fahrt, auf die sich die Anfrage bezieht
    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    // Status: PENDING, ACCEPTED, REJECTED
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    // optionale Nachricht vom Mitfahrer
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- GETTER & SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
