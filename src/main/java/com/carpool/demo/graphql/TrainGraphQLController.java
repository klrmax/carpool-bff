package com.carpool.demo.graphql;

//Funktioniert nicht

import com.carpool.demo.data.api.TrainManager;
import com.carpool.demo.model.train.TrainConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TrainGraphQLController {

    private final TrainManager trainManager;

    @Autowired
    public TrainGraphQLController(TrainManager trainManager) {
        this.trainManager = trainManager;
    }

    @QueryMapping
    public List<TrainConnection> getTrainConnections(
            @Argument String from,
            @Argument String to,
            @Argument String date,
            @Argument String hour
    ) {
        // ÖFFENTLICH - kein Token erforderlich (wie REST /api/trains)

        //  Falls kein Datum/Zeit angegeben → jetzt
        LocalDateTime now = LocalDateTime.now();
        if (date == null || date.isEmpty()) {
            date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        if (hour == null || hour.isEmpty()) {
            hour = now.format(DateTimeFormatter.ofPattern("HH"));
        }

        //  Auto-Korrektur für häufige Bahnhofsnamen
        from = normalizeStationName(from);
        to = normalizeStationName(to);

        String rawJson = trainManager.getTrainData(from, to, date, hour);
        JSONObject json = new JSONObject(rawJson);

        if (json.has("error")) {
            throw new RuntimeException(json.getString("error"));
        }

        //  Konvertiere Ergebnis in Java-Objekte
        List<TrainConnection> result = new ArrayList<>();
        if (json.has("connections")) {
            JSONArray connections = json.getJSONArray("connections");
            for (int i = 0; i < connections.length(); i++) {
                JSONObject c = connections.getJSONObject(i);
                TrainConnection conn = new TrainConnection();
                conn.setTrainNumber(c.optString("trainNumber"));
                conn.setDirection(c.optString("path"));
                conn.setPlannedTime(c.optString("departureTime"));
                conn.setPlannedPlatform(c.optString("platform"));
                conn.setStationEva(json.optString("fromEva"));
                conn.setStationName(from);
                conn.setType("bahn");
                result.add(conn);
            }
        }

        return result;
    }

    //  Kleine Helper-Methode zur Vereinheitlichung der Bahnhofsnamen
    private String normalizeStationName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();

        if (!lower.contains("hbf") && (
                lower.contains("stuttgart") || lower.contains("berlin") ||
                        lower.contains("münchen") || lower.contains("frankfurt") ||
                        lower.contains("hamburg") || lower.contains("köln") ||
                        lower.contains("düsseldorf") || lower.contains("leipzig")
        )) {
            return name + " Hbf";
        }

        return name;
    }
}
