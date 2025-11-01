package com.carpool.demo.model.ride;


import com.carpool.demo.model.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "rides")
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonProperty("departure_location")
    @Column(nullable = false)
    private String startLocation;

    @JsonProperty("destination_location")
    @Column(nullable = false)
    private String destination;

    @JsonProperty("departure_time")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @JsonProperty("seats_available")
    private int availableSeats;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User driver;

    // Optional zusätzliche Felder für API-Kompatibilität
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("date")
    private LocalDateTime date;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    // ---- Getter & Setter ----

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }


    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public  LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

}
