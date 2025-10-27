package com.carpool.demo.data.api;

public interface TrainManager {
    /**
     * Holt Zugverbindungen basierend auf Start/Ziel/Datum/Uhrzeit
     *
     * @param start       Startbahnhof (Name, z.B. "Berlin Hbf")
     * @param destination Zielbahnhof (Name, z.B. "Hamburg Hbf")
     * @param date        Datum im Format YYYYMMDD (z.B. 20251028)
     * @param hour        Stunde im Format HH (z.B. 10)
     * @return JSON mit Bahnverbindungen
     */
    String getTrainData(String start, String destination, String date, String hour);
}
