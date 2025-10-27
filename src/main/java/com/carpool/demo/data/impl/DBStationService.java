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
        try {
            // Versuche erst den Original-Namen, dann mit " Hbf" falls keine Ergebnisse
            String searchName = stationName;

            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + searchName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("DB-Client-Id", CLIENT_ID);
            headers.set("DB-Api-Key", API_KEY);
            headers.set("Accept", "application/xml");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String xml = response.getBody();
            if (xml == null || xml.isEmpty()) {
                throw new RuntimeException("Leere Antwort von DB Station API für: " + searchName);
            }

            JSONObject json = XML.toJSONObject(xml);

            // Prüfe verschiedene mögliche Strukturen
            // 1. stations.station (aktuelles Format)
            if (json.has("stations")) {
                JSONObject stations = json.getJSONObject("stations");
                if (stations.has("station")) {
                    Object stationObj = stations.get("station");

                    if (stationObj instanceof JSONArray) {
                        JSONArray stationArr = (JSONArray) stationObj;

                        // 1. Versuch: Bevorzuge "Hbf" für bekannte Großstädte
                        String lowerSearch = stationName.toLowerCase();
                        boolean isLargeCity = lowerSearch.contains("münchen") || lowerSearch.contains("muenchen") ||
                                lowerSearch.contains("berlin") || lowerSearch.contains("hamburg") ||
                                lowerSearch.contains("köln") || lowerSearch.contains("koeln") ||
                                lowerSearch.contains("frankfurt") || lowerSearch.contains("stuttgart");

                        if (isLargeCity) {
                            // Bei Großstädten: Finde exakt "Hbf" ohne Klammern
                            for (int i = 0; i < stationArr.length(); i++) {
                                JSONObject station = stationArr.getJSONObject(i);
                                String name = station.optString("name", "");
                                if (name.endsWith(" Hbf") && !name.contains("(")) {
                                    return String.valueOf(station.getInt("eva"));
                                }
                            }
                        }

                        // 2. Versuch: Finde Station die am besten zum Suchnamen passt (ohne Zusätze)
                        for (int i = 0; i < stationArr.length(); i++) {
                            JSONObject station = stationArr.getJSONObject(i);
                            String name = station.optString("name", "");
                            // Exakte Übereinstimmung oder nur Bahnhof ohne Zusatz
                            if (name.equalsIgnoreCase(stationName) ||
                                    (name.toLowerCase().startsWith(stationName.toLowerCase()) && !name.contains("("))) {
                                return String.valueOf(station.getInt("eva"));
                            }
                        }

                        // 3. Versuch: Kleinste EVA ohne Zusätze (oft Hauptstation)
                        int selectedEva = Integer.MAX_VALUE;
                        for (int i = 0; i < stationArr.length(); i++) {
                            JSONObject station = stationArr.getJSONObject(i);
                            int eva = station.getInt("eva");
                            String name = station.optString("name", "");
                            if (!name.contains("(") && eva < selectedEva) {
                                selectedEva = eva;
                            }
                        }
                        if (selectedEva != Integer.MAX_VALUE) {
                            return String.valueOf(selectedEva);
                        }

                        // Fallback: erste Station
                        JSONObject firstStation = stationArr.getJSONObject(0);
                        return String.valueOf(firstStation.getInt("eva"));
                    } else if (stationObj instanceof JSONObject) {
                        JSONObject station = (JSONObject) stationObj;
                        String name = station.optString("name", "");
                        int eva = station.getInt("eva");

                        // Wenn es der Tiefbahnhof ist und meta-Feld existiert, nutze die erste meta-EVA
                        if (name.contains("(tief)") && station.has("meta")) {
                            String meta = station.getString("meta");
                            String[] metaEvas = meta.split("\\|");
                            if (metaEvas.length > 0) {
                                return metaEvas[0];
                            }
                        }

                        return String.valueOf(eva);
                    }
                }
            }

            // 2. multipleStationData.station (alternatives Format)
            if (json.has("multipleStationData")) {
                JSONObject multipleStationData = json.getJSONObject("multipleStationData");
                if (multipleStationData.has("station")) {
                    Object stationObj = multipleStationData.get("station");

                    if (stationObj instanceof JSONArray) {
                        return ((JSONArray) stationObj).getJSONObject(0).getString("eva");
                    } else if (stationObj instanceof JSONObject) {
                        return ((JSONObject) stationObj).getString("eva");
                    }
                }
            }

            // 3. Fallback: Wenn es direkt ein station-Objekt gibt
            if (json.has("station")) {
                Object stationObj = json.get("station");
                if (stationObj instanceof JSONArray) {
                    return ((JSONArray) stationObj).getJSONObject(0).getString("eva");
                } else if (stationObj instanceof JSONObject) {
                    return ((JSONObject) stationObj).getString("eva");
                }
            }

            // Wenn nichts gefunden wurde, gebe die komplette Antwort für Debugging aus
            throw new RuntimeException("Keine gültige Station gefunden für: " + searchName +
                    ". API Antwort: " + json.toString(2));

        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Auflösen der Station '" + stationName + "': " + e.getMessage());
        }
    }
}
