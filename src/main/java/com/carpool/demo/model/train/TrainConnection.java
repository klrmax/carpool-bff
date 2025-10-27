package com.carpool.demo.model.train;

public class TrainConnection {
    private String trainNumber;      // z.B. "ICE 578"
    private String direction;        // "Berlin Hbf"
    private String plannedTime;      // ISO-String: 2025-12-25T09:12:00
    private String plannedPlatform;  // z.B. "8"
    private String stationEva;       // z.B. "8000157"
    private String stationName;      // optional
    private String type = "bahn";    // zur Unterscheidung

    // getters/setters
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getPlannedTime() { return plannedTime; }
    public void setPlannedTime(String plannedTime) { this.plannedTime = plannedTime; }
    public String getPlannedPlatform() { return plannedPlatform; }
    public void setPlannedPlatform(String plannedPlatform) { this.plannedPlatform = plannedPlatform; }
    public String getStationEva() { return stationEva; }
    public void setStationEva(String stationEva) { this.stationEva = stationEva; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
