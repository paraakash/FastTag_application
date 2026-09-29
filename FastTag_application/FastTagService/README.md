# FastTagService

Minimal Spring Boot service for user authentication.

Run:

```bash
cd FastTagService
mvn spring-boot:run
```

Configuration: edit `src/main/resources/application.properties` to set Oracle and Redis connection properties.

Notes:
 The service now issues JWT tokens (24h) and stores them in Redis with a 24-hour expiry.
 - To enable HTTPS with a self-signed certificate (development):
	1. Create a PKCS12 keystore:

```bash
keytool -genkeypair -alias fasttag -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

	2. Place `keystore.p12` under `src/main/resources` and update `src/main/resources/application.properties` with `server.ssl.key-store=classpath:keystore.p12` and `server.ssl.key-store-password`.
