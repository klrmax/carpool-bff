package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.TrainManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.XML;

@Service
public class DBTrainManagerImpl implements TrainManager {

    // RICHTIGER BASE_URL
    private static final String BASE_URL = "https://api.deutschebahn.com/timetables/v1/fchg/";

    private static final String CLIENT_ID = "e427eb3913aff7deeb86d9d9029dafbe";
    private static final String API_KEY = "36cc3e71c5bf83d490f25532192aded2";

    @Override
    public String getTrainData(String evaNo, String date, String hour) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("DB-Client-Id", CLIENT_ID);
        headers.set("DB-Api-Key", API_KEY);
        headers.set("Accept", "application/xml");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = BASE_URL + evaNo; // z. B. 8000157/2025-12-25/16

        //System.out.println("Sende Request an: " + url);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String xml = response.getBody();
            if (xml == null || xml.isEmpty()) {
                throw new RuntimeException("Leere Antwort von DB API");
            }

            JSONObject json = XML.toJSONObject(xml);
            return json.toString(2);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler bei DB API: " + e.getMessage());
        }
    }
}
