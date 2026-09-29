# CachingService

Spring Boot service that loads `toll_master` and `transaction_balance` data into Redis and refreshes it every 10 minutes.

## Run locally

```bash
cd CachingService
mvn spring-boot:run
```

## Configuration

Update `src/main/resources/application.properties` with your Oracle and Redis values:

```properties
oracle.url=jdbc:oracle:thin:@//localhost:1521/ORCLCDB
oracle.user=fasttag_user
oracle.password=fasttag_pass
spring.redis.host=localhost
spring.redis.port=6379
cache.refresh.interval-ms=600000
```

## Behavior

- Full load at startup for both `toll_master` and `transaction_balance`
- Incremental refresh every 10 minutes based on `modified_dt` / `ceated_dt`
- Redis keys use:
  - `toll:<Tollplaza_name>:<vehicle_type>` for toll amount lookup
  - `vehicle:<Vehocle_registration_nbr>` for vehicle type lookup

## Example endpoints

```http
GET /cache/toll?Tollplaza_name=...&vehicle_type=...
GET /cache/vehicle?Vehicle_registration_nbr=...
GET /cache/refresh
```
