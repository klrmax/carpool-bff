package com.carpool.demo.controller;

import com.carpool.demo.data.api.TrainManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    @Autowired
    private TrainManager trainManager;

    @GetMapping
    public ResponseEntity<String> getTrains(
            @RequestParam String start,
            @RequestParam String destination,
            @RequestParam String date,
            @RequestParam String hour) {

        String trains = trainManager.getTrainData(start, destination, date, hour);
        return ResponseEntity.ok(trains);
    }
}
