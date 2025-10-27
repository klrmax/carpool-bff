package com.carpool.demo.data.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.XML;
import org.json.JSONObject;
import org.json.JSONArray;

@Service
public class DBStationService {

    private static final String BASE_URL = "https://apis.deutschebahn.com/db-api-marketplace/apis/timetables/v1/station/";
    private static final String CLIENT_ID = "e427eb3913aff7deeb86d9d9029dafbe";
    private static final String API_KEY = "36cc3e71c5bf83d490f25532192aded2";

    public String getEVA(String stationName) {
        RestTemplate restTemplate = new RestTemplate();
        String url = BASE_URL + stationName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("DB-Client-Id", CLIENT_ID);
        headers.set("DB-Api-Key", API_KEY);
        headers.set("Accept", "application/xml");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        String xml = response.getBody();
        if (xml == null || xml.isEmpty()) {
            throw new RuntimeException("Leere Antwort von DB Station API");
        }

        JSONObject json = XML.toJSONObject(xml);
        JSONObject multipleStationData = json.getJSONObject("multipleStationData");
        Object stationObj = multipleStationData.get("station");

        if (stationObj instanceof JSONArray) {
            return ((JSONArray) stationObj).getJSONObject(0).getString("eva");
        } else if (stationObj instanceof JSONObject) {
            return ((JSONObject) stationObj).getString("eva");
        } else {
            throw new RuntimeException("Keine gültige Station gefunden für: " + stationName);
        }
    }
}
