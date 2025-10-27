package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.TrainManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DBTrainManagerImpl implements TrainManager {

    @Autowired
    private DBStationService dbStationService;

    private static final String BASE_URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/plan/";
    private static final String CLIENT_ID = "e427eb3913aff7deeb86d9d9029dafbe";
    private static final String API_KEY = "36cc3e71c5bf83d490f25532192aded2";

    @Override
    public String getTrainData(String start, String destination, String date, String hour) {
        // EVA-Codes für Start/Ziel holen
        String evaStart = dbStationService.getEVA(start);
        String evaDestination = dbStationService.getEVA(destination);

        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + evaStart + "/" + date + "/" + hour;

        HttpHeaders headers = new HttpHeaders();
        headers.set("DB-Client-Id", CLIENT_ID);
        headers.set("DB-Api-Key", API_KEY);
        headers.set("Accept", "application/xml");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        String xml = response.getBody();
        if (xml == null || xml.isEmpty()) {
            throw new RuntimeException("Leere Antwort von DB Timetable API");
        }

        JSONObject json = XML.toJSONObject(xml);
        JSONObject wrapper = new JSONObject();
        wrapper.put("type", "train");
        wrapper.put("startStation", start);
        wrapper.put("destinationStation", destination);
        wrapper.put("data", json);

        return wrapper.toString(2);
    }
}
