# Bus Booking Service

Java 17+ REST service using the generated Spring Boot 4.1.2 version, Spring Data JPA,
H2, Bean Validation and Springdoc 3.2.0. All application packages live under
`com.shank.booking_service`. Controllers use DTOs; transactional services own pricing
and cancellation. No bus inventory, payments or authentication are included.

## Run On Windows

From this directory (the directory containing `pom.xml` and `mvnw.cmd`):

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
java -jar target\booking-service2-0.0.1-SNAPSHOT.jar
```

Alternatively run ` .\mvnw.cmd spring-boot:run` for development.
The service listens at http://localhost:8082.

- Swagger UI: http://localhost:8082/swagger-ui.html
- OpenAPI JSON: http://localhost:8082/v3/api-docs
- H2 console: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:file:./data/busdb;MODE=PostgreSQL`
- Driver: `org.h2.Driver`, username: `sa`, password: empty

The file database is relative to the startup directory and persists across restarts.
These settings and the enabled H2 console are for local development only.
Tests use a separate in-memory database with schema recreation, never `data/busdb`.
Price per seat defaults to `500.00`; override `booking.price-per-seat` through Spring
configuration, for example `--booking.price-per-seat=650.00` when starting the jar.

## Requests

PowerShell examples below use today's date so validation remains valid.
The request intentionally uses `jouneyDate`; responses use `journeyDate`.

### Create: POST /api/booking/createBooking

```powershell
$body = @{
    busId = 12
    passengerName = 'Test Passenger'
    passengerEmail = 'test@example.com'
    passengerPhone = '1234567890'
    jouneyDate = (Get-Date -Format 'yyyy-MM-dd')
    numberOfSeats = 2
} | ConvertTo-Json
$booking = Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/api/booking/createBooking' -ContentType 'application/json' -Body $body
$booking
```

Returns HTTP 201 and all booking fields, including a generated `id`, server-calculated
`totalAmount` (1000.00 for two seats) and `bookingStatus` of `PENDING`.
Client-supplied price and status do not control the booking.

### List: GET /api/booking/getAllBookings

```powershell
Invoke-RestMethod 'http://localhost:8082/api/booking/getAllBookings'
```

Returns HTTP 200 with a JSON array, or `[]` if no bookings exist.

### Lookup: GET /api/booking/{id}

```powershell
Invoke-RestMethod "http://localhost:8082/api/booking/$($booking.id)"
```

### Cancel: PUT /api/booking/{id}/cancel

```powershell
Invoke-RestMethod -Method Put -Uri "http://localhost:8082/api/booking/$($booking.id)/cancel"
```

Lookup and cancellation return HTTP 200 with a booking, or HTTP 404 when missing.
Cancellation persists `CANCELLED` without deletion and can be repeated successfully.
`CONFIRMED` is a supported enum value only; there is no confirmation endpoint.

## Errors And Verification

Errors include `timestamp`, `status`, `error` and `message`. Validation errors are
HTTP 400 with message `Validation failed` and a `details` field-to-message map.
Malformed JSON, invalid dates and invalid path IDs return HTTP 400 with message
`Malformed request`. Missing IDs return `Booking not found with id: {id}`.
Unexpected failures return HTTP 500 with `Unexpected error occurred`, without stack traces.

The Maven test suite exercises creation, persisted price/status, response spelling,
empty/populated lists, lookup, missing IDs, persistent repeated cancellation,
required/invalid fields and malformed bodies/path variables against isolated H2.