### API Endpoints
### Authentication
- POST /api/users/register - User registrieren
- POST /api/users/login - User einloggen
- POST /api/users/logout - User ausloggen

### Rides
- GET /api/ride - Alle Fahrten (Token required)
- GET /api/ride/search?departure_location=X&destination_location=Y&date=Z&time=T - Fahrten suchen (Token required)
- GET /api/ride/{id} - Einzelne Fahrt (Token required)
- POST /api/ride - Neue Fahrt erstellen (Token required)
- GET /api/ride/mine - Meine Fahrten (Token required)
- GET /api/ride/joined - Fahrten wo ich Mitfahrer bin (Token required)
- DELETE /api/ride/{id} - Fahrt löschen (Token required)

### Ride Requests (Mitfahranfragen)
- POST /api/ride-request - Mitfahranfrage erstellen (Token required) Query Parameter ride ID
- GET /api/ride-request/open - Offene Anfragen für meine Fahrten (Token required)
- GET /api/ride-request/mine - Meine gesendeten Anfragen (Token required)
- PATCH /api/ride-request/{id} - Anfrage annehmen/ablehnen (Token required)

### Deutsche Bahn Integration
- GET /api/trains?start=Frankfurt&destination=München&date=251030&hour=14


### // JavaScript Beispiel für das Token Handling
fetch('https://carpoolbff-c576f25b03e8.herokuapp.com/api/ride', {
method: 'POST',
headers: {
'Content-Type': 'application/json',
'Authorization': 'Bearer ' + token  // ← So!
},
body: JSON.stringify(rideData)
}) 