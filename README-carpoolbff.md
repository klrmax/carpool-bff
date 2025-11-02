# Carpool Backend (BFF)

A RESTful and GraphQL API backend for a carpool application built with Spring Boot.

## Overview

This backend provides user authentication, ride management, ride request handling, and external Deutsche Bahn train connection integration. It supports both REST and GraphQL APIs and includes features like JWT authentication, caching, async processing, and background notifications.

## Tech Stack

- Java 21
- Spring Boot 3.5.4
- Spring Data JPA
- PostgreSQL
- GraphQL
- JWT Authentication
- BCrypt Password Hashing
- Async Processing
- Caching

## Project Structure

```
src/main/java/com/carpool/demo/
├── config/          - Application configuration (Security, CORS, AsyncConfig, etc.)
├── controller/      - REST API controllers
├── data/
│   ├── api/         - Manager interfaces (business logic layer)
│   ├── impl/        - Manager implementations
│   └── repository/  - JPA repositories
├── graphql/         - GraphQL controllers
├── model/           - Entity models (User, Ride, RideRequest, Train)
├── security/        - JWT authentication filter
├── utils/           - Utility classes (JwtUtils, AuthUtils, UserCache)
└── exception/       - Custom exceptions
```

## Features

### Authentication
- User registration with password hashing
- JWT-based login and token validation
- Token-based authorization for protected endpoints

### Ride Management
- Create, read, search, and delete rides
- Search by location, date, and time
- View own rides and joined rides
- Parallel search with CompletableFuture

### Ride Requests
- Create ride join requests
- Accept or reject requests (driver only)
- View open requests for own rides
- View own sent requests

### External Integration
- Deutsche Bahn API integration for train connections

### Advanced Features
- Async processing with ThreadPoolTaskExecutor
- Caching for improved performance
- Background notification queue
- Scheduled tasks

## API Endpoints

### Authentication
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - User login (returns JWT token)
- `POST /api/users/logout` - User logout

### Rides
- `GET /api/ride` - Get all rides (public)
- `GET /api/ride/search` - Search rides by location, date, time (public)
  - Query params: `start`, `destination`, `date`, `time`
- `GET /api/ride/{id}` - Get ride by ID (requires token)
- `POST /api/ride` - Create new ride (requires token)
- `GET /api/ride/mine` - Get own rides as driver (requires token)
- `GET /api/ride/joined` - Get rides as passenger (requires token)
- `DELETE /api/ride/{id}` - Delete ride (requires token)

### Ride Requests
- `POST /api/ride-request` - Create ride request (requires token)
  - Query param: `rideId`
- `GET /api/ride-request/open` - Get open requests for own rides (requires token)
- `GET /api/ride-request/mine` - Get own sent requests (requires token)
- `PATCH /api/ride-request/{id}` - Accept/reject request (requires token)

### Deutsche Bahn Integration
- `GET /api/trains` - Get train connections
  - Query params: `start`, `destination`, `date`, `hour`

## GraphQL API

The GraphQL endpoint is available at `/graphql`.

### Queries
- `getRideById(id: ID!)` - Get ride by ID
- `searchRides(start, destination, date, time)` - Search for rides
- `getAllRides` - Get all rides
- `getRideRequests` - Get own ride requests (as passenger, requires token)
- `getOpenRideRequests` - Get open requests for own rides (as driver, requires token)
- `getTrainConnections(from, to, date, hour)` - Get train connections

### Mutations
- `register(name, mobileNumber, password)` - Register user
- `login(mobileNumber, password)` - Login user
- `createRide(startLocation, destination, departureTime, availableSeats)` - Create ride (requires token)
- `createRideRequest(rideId, message)` - Create ride request (requires token)
- `changeRideRequestStatus(requestId, status)` - Accept/reject request (requires token)

## Authentication Example

Include the JWT token in the Authorization header for protected endpoints:

```javascript
fetch('https://carpoolbff-c576f25b03e8.herokuapp.com/api/ride', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify(rideData)
})
```

For GraphQL, pass the token via HTTP headers:

```javascript
fetch('https://carpoolbff-c576f25b03e8.herokuapp.com/graphql', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': token
  },
  body: JSON.stringify({
    query: `mutation { createRide(...) { id } }`
  })
})
```

## Environment Configuration

The application uses PostgreSQL database. Configure connection in `application.properties`:

```properties
spring.datasource.url=${JDBC_DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## Deployment

This application is configured for Heroku deployment.

```bash
git push heroku main
```

## License

This project is licensed under the MIT License.
