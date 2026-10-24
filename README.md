# Smart Parking Lot System

A Spring Boot REST API for managing a multi-floor parking lot. It supports parking spot creation, automatic size-compatible allocation, check-in/check-out, fee calculation, and live availability queries.

## Tech stack
- Java 21
- Spring Boot 3.5.x
- Spring Web, Spring Data JPA, Jakarta Bean Validation
- H2 database (file-backed for local persistence)
- JUnit 5

## Run locally

Prerequisites: JDK 21 and Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

Application base URL: `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/parkingdb`, user `sa`, blank password).

## API quick start

Create spots (spot types represent capacity: MOTORCYCLE < CAR < BUS):

```bash
curl -X POST http://localhost:8080/api/spots -H "Content-Type: application/json" -d '{"floorNumber":0,"spotNumber":"M-001","type":"MOTORCYCLE"}'
curl -X POST http://localhost:8080/api/spots -H "Content-Type: application/json" -d '{"floorNumber":0,"spotNumber":"C-001","type":"CAR"}'
curl -X POST http://localhost:8080/api/spots -H "Content-Type: application/json" -d '{"floorNumber":1,"spotNumber":"B-001","type":"BUS"}'
```

Check a vehicle in:

```bash
curl -X POST http://localhost:8080/api/parking/check-in -H "Content-Type: application/json" -d '{"registrationNumber":"TS09AB1234","type":"CAR"}'
```

Use the returned `ticketId` to check out:

```bash
curl -X POST http://localhost:8080/api/parking/1/check-out
```

Other endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/spots` | Create a spot |
| GET | `/api/spots` | List all spots |
| GET | `/api/spots?availableOnly=true` | List available spots |
| GET | `/api/availability` | Current availability snapshot |
| POST | `/api/parking/check-in` | Register/find vehicle and allocate a spot |
| POST | `/api/parking/{ticketId}/check-out` | Close ticket, calculate fee, release spot |
| GET | `/api/parking/active` | List active tickets |

## Allocation and concurrency design

1. Candidate spot IDs are read in deterministic order: smallest compatible capacity first, then floor and spot number.
2. The candidate query is only a discovery step; it does **not** reserve the spot.
3. For each candidate, the service runs a conditional database update equivalent to `UPDATE parking_spots SET status='OCCUPIED' WHERE id=? AND status='AVAILABLE'`.
4. Exactly one competing transaction can change a given row from `AVAILABLE` to `OCCUPIED`. If an update affects zero rows, another request won the race and this request tries the next candidate.
5. The spot claim and parking-ticket insert run in one `@Transactional` method. If ticket creation fails, the transaction rolls back the claim.
6. A pessimistic lock on an existing vehicle row serializes concurrent check-ins for the same registered vehicle; the active-ticket check is repeated while holding that lock.
7. Checkout calculates the fee, closes the ticket, and releases the spot in a single transaction. The conditional release prevents a stale ticket from releasing a spot that is not occupied.

This is database-backed coordination, unlike Java `synchronized`, which only coordinates threads inside one JVM. The exact SQL locking behavior should be validated against the chosen production database; H2 is used here for local development. For production, use PostgreSQL/MySQL with appropriate integration/concurrency tests, migrations, authentication, and operational configuration.

## Fee policy

Demo tariff is per started hour, with a one-hour minimum:
- Motorcycle: ₹20/hour
- Car: ₹50/hour
- Bus: ₹100/hour

The tariff is intentionally isolated in `FeeCalculator` so it can be changed without modifying the check-in/check-out orchestration.

## Design notes

- `ParkingController`: HTTP boundary and DTO mapping.
- `ParkingService`: transactional use cases and orchestration.
- `ParkingSpot`, `Vehicle`, `ParkingTicket`: persistence/domain state.
- Repositories: database access, including atomic conditional spot claims.
- `FeeCalculator`: pricing policy.
- DTO records keep request/response contracts separate from JPA entities.

## Scope / limitations

This is a focused assignment implementation, not a full commercial parking platform. It does not include authentication/authorization, payment integration, reservations, dynamic tariff configuration, audit/event streaming, or a dedicated idempotency-key API. A simultaneous duplicate request for a *new* vehicle registration can be rejected by the registration uniqueness constraint; clients should treat conflict responses as retryable only after checking current state. Add migrations (Flyway/Liquibase) and database-specific concurrency tests before production deployment.
