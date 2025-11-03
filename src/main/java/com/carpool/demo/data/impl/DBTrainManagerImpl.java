package com.carpool.demo.data.impl;

import com.carpool.demo.data.api.TrainManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.json.JSONObject;
import org.json.JSONArray;
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
        try {
            // Validiere und konvertiere Datum ins DB API Format (YYMMDD)
            String dbDate = convertToDBDateFormat(date);
            
            // Validiere Stunde (00-23)
            String dbHour = validateHour(hour);
            
            // EVA-Codes für Start/Ziel holen
            String evaStart = dbStationService.getEVA(start);
            String evaDestination = dbStationService.getEVA(destination);

            // Hole Fahrplandaten für die Startstation
            RestTemplate restTemplate = new RestTemplate();
            String url = BASE_URL + evaStart + "/" + dbDate + "/" + dbHour;
            
            System.out.println("DEBUG: DB API URL: " + url);

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

            JSONObject timetable = XML.toJSONObject(xml);

            // Filtere nach Verbindungen, die zum Ziel fahren
            JSONArray connections = new JSONArray();

            if (timetable.has("timetable")) {
                JSONObject tt = timetable.getJSONObject("timetable");
                if (tt.has("s")) {
                    Object sObj = tt.get("s");

                    // Behandle sowohl einzelne als auch mehrere Einträge
                    JSONArray entries = new JSONArray();
                    if (sObj instanceof JSONArray) {
                        entries = (JSONArray) sObj;
                    } else if (sObj instanceof JSONObject) {
                        entries.put(sObj);
                    }

                    // Durchsuche alle Einträge nach Verbindungen zum Ziel
                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject entry = entries.getJSONObject(i);

                        // Prüfe ob diese Verbindung zum Ziel fährt (ppth = planned path)
                        String plannedPath = null;
                        if (entry.has("dp")) {
                            JSONObject dp = entry.getJSONObject("dp");
                            if (dp.has("ppth")) {
                                plannedPath = dp.getString("ppth");
                            }
                        }

                        // Flexiblere Suche: Prüfe auf Teilstring-Match (case-insensitive)
                        boolean matchesDestination = false;
                        if (plannedPath != null) {
                            String lowerPath = plannedPath.toLowerCase();
                            String lowerDest = destination.toLowerCase();

                            // Extrahiere Kernname (z.B. "München" aus "München Hbf")
                            String destCore = lowerDest.split(" ")[0];

                            matchesDestination = lowerPath.contains(lowerDest) ||
                                    lowerPath.contains(destCore) ||
                                    plannedPath.contains(evaDestination);
                        }

                        if (matchesDestination) {
                            JSONObject connection = new JSONObject();

                            // Zuginformationen
                            if (entry.has("tl")) {
                                JSONObject tl = entry.getJSONObject("tl");
                                connection.put("trainCategory", tl.optString("c", ""));
                                connection.put("trainNumber", tl.optString("n", ""));
                                connection.put("trainType", tl.optString("t", ""));
                            }

                            // Abfahrtsinformationen
                            if (entry.has("dp")) {
                                JSONObject dp = entry.getJSONObject("dp");
                                connection.put("departureTime", dp.optString("pt", ""));
                                connection.put("platform", dp.optString("pp", ""));
                                connection.put("path", dp.optString("ppth", ""));
                            }

                            connection.put("id", entry.optString("id", ""));
                            connections.put(connection);
                        }
                    }
                }
            }

            JSONObject result = new JSONObject();
            result.put("from", start);
            result.put("fromEva", evaStart);
            result.put("to", destination);
            result.put("toEva", evaDestination);
            result.put("date", date);
            result.put("requestedDate", dbDate);
            result.put("hour", hour);
            result.put("connections", connections);
            result.put("totalConnections", connections.length());

            // Füge hilfreiche Nachricht hinzu, wenn keine Verbindungen gefunden wurden
            if (connections.length() == 0) {
                result.put("message", "Keine direkten Verbindungen gefunden für " + date + " um " + hour + " Uhr. " +
                        "Hinweis: Die DB API hat nur Fahrplandaten für die nächsten 2-3 Wochen.");
            }

            return result.toString(2);

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            error.put("from", start);
            error.put("to", destination);
            error.put("date", date);
            error.put("hour", hour);
            System.err.println("DB API Error: " + e.getMessage());
            e.printStackTrace();
            return error.toString(2);
        }
    }
    
    /**
     * Konvertiert verschiedene Datumsformate ins DB API Format (YYMMDD)
     * Unterstützt: YYYY-MM-DD, DD.MM.YYYY, YYMMDD
     */
    private String convertToDBDateFormat(String date) {
        try {
            // Entferne alle Separatoren
            String cleaned = date.replaceAll("[.\\-/]", "");
            
            // Format: YYYYMMDD (8 Zeichen) -> YYMMDD (6 Zeichen)
            if (cleaned.length() == 8) {
                String year = cleaned.substring(2, 4);  // YY
                String month = cleaned.substring(4, 6);  // MM
                String day = cleaned.substring(6, 8);    // DD
                return year + month + day;
            }
            
            // Format: DDMMYYYY (8 Zeichen) -> YYMMDD (6 Zeichen)
            if (cleaned.length() == 8 && Integer.parseInt(cleaned.substring(0, 2)) <= 31) {
                String day = cleaned.substring(0, 2);    // DD
                String month = cleaned.substring(2, 4);  // MM
                String year = cleaned.substring(6, 8);   // YY
                return year + month + day;
            }
            
            // Format: YYMMDD (bereits korrekt)
            if (cleaned.length() == 6) {
                return cleaned;
            }
            
            throw new IllegalArgumentException(
                "Ungültiges Datumsformat. Unterstützt: YYYY-MM-DD, DD.MM.YYYY, YYMMDD. Erhalten: " + date
            );
            
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Fehler beim Konvertieren des Datums '" + date + "': " + e.getMessage()
            );
        }
    }
    
    /**
     * Validiert die Stunde (00-23) und formatiert sie mit führender Null
     */
    private String validateHour(String hour) {
        try {
            int h = Integer.parseInt(hour);
            if (h < 0 || h > 23) {
                throw new IllegalArgumentException("Stunde muss zwischen 00 und 23 liegen");
            }
            return String.format("%02d", h);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ungültige Stunde: " + hour);
        }
    }
}
