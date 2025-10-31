package com.carpool.demo.graphql;

import com.carpool.demo.data.api.TrainManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TrainGraphQLController {

    private final TrainManager trainManager;

    @Autowired
    public TrainGraphQLController(TrainManager trainManager) {
        this.trainManager = trainManager;
    }

    @QueryMapping
    public String getTrainConnections(
            @Argument String from,
            @Argument String to,
            @Argument String date,  // optional in GraphQL, kein required nötig
            @Argument String hour   // optional in GraphQL
    ) {
        // Delegiert an das bestehende Interface
        return trainManager.getTrainData(from, to, date, hour);
    }
}
