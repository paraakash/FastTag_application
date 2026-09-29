package com.fasttag.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class RedisCacheLoaderService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheLoaderService.class);

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.toll.key-prefix:toll}")
    private String tollKeyPrefix;

    @Value("${cache.vehicle.key-prefix:vehicle}")
    private String vehicleKeyPrefix;

    @Value("${cache.toll.last-sync-key:toll:last_sync}")
    private String tollLastSyncKey;

    @Value("${cache.vehicle.last-sync-key:vehicle:last_sync}")
    private String vehicleLastSyncKey;

    public RedisCacheLoaderService(JdbcTemplate jdbcTemplate, RedisTemplate<String, String> redisTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initialize() {
        loadFullTollMasterCache();
        loadFullVehicleBalanceCache();
    }

    @Scheduled(fixedDelayString = "${cache.refresh.interval-ms:600000}")
    public void refreshIncrementalCaches() {
        loadIncrementalTollMasterCache();
        loadIncrementalVehicleBalanceCache();
    }

    public void loadFullTollMasterCache() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT Tollplaza_name, vehicle_type, toll_amt FROM toll_master"
        );
        for (Map<String, Object> row : rows) {
            String key = tollKeyPrefix + ":" + row.get("Tollplaza_name") + ":" + row.get("vehicle_type");
            redisTemplate.opsForValue().set(key, String.valueOf(row.get("toll_amt")));
        }
        redisTemplate.opsForValue().set(tollLastSyncKey, Timestamp.from(Instant.now()).toString());
        log.info("Loaded {} toll entries into Redis", rows.size());
    }

    public void loadIncrementalTollMasterCache() {
        String lastSync = redisTemplate.opsForValue().get(tollLastSyncKey);
        if (lastSync == null || lastSync.isBlank()) {
            loadFullTollMasterCache();
            return;
        }

        Timestamp ts = Timestamp.valueOf(lastSync);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT Tollplaza_name, vehicle_type, toll_amt, ceated_dt, modified_dt FROM toll_master " +
                        "WHERE modified_dt >= ? OR ceated_dt >= ? ORDER BY NVL(modified_dt, ceated_dt), NVL(ceated_dt, modified_dt)",
                ts, ts
        );

        for (Map<String, Object> row : rows) {
            String key = tollKeyPrefix + ":" + row.get("Tollplaza_name") + ":" + row.get("vehicle_type");
            redisTemplate.opsForValue().set(key, String.valueOf(row.get("toll_amt")));
        }
        redisTemplate.opsForValue().set(tollLastSyncKey, Timestamp.from(Instant.now()).toString());
        log.info("Loaded {} incremental toll entries into Redis", rows.size());
    }

    public void loadFullVehicleBalanceCache() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT Vehocle_registration_nbr, vehicle_type, ceated_dt, modified_dt FROM transaction_balance"
        );
        for (Map<String, Object> row : rows) {
            String key = vehicleKeyPrefix + ":" + row.get("Vehocle_registration_nbr");
            redisTemplate.opsForValue().set(key, String.valueOf(row.get("vehicle_type")));
        }
        redisTemplate.opsForValue().set(vehicleLastSyncKey, Timestamp.from(Instant.now()).toString());
        log.info("Loaded {} vehicle-type entries into Redis", rows.size());
    }

    public void loadIncrementalVehicleBalanceCache() {
        String lastSync = redisTemplate.opsForValue().get(vehicleLastSyncKey);
        if (lastSync == null || lastSync.isBlank()) {
            loadFullVehicleBalanceCache();
            return;
        }

        Timestamp ts = Timestamp.valueOf(lastSync);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT Vehocle_registration_nbr, vehicle_type, ceated_dt, modified_dt FROM transaction_balance " +
                        "WHERE  ceated_dt >= ? ORDER BY NVL(modified_dt, ceated_dt), NVL(ceated_dt, modified_dt)",
                 ts
        );

        for (Map<String, Object> row : rows) {
            String key = vehicleKeyPrefix + ":" + row.get("Vehocle_registration_nbr");
            redisTemplate.opsForValue().set(key, String.valueOf(row.get("vehicle_type")));
        }
        redisTemplate.opsForValue().set(vehicleLastSyncKey, Timestamp.from(Instant.now()).toString());
        log.info("Loaded {} incremental vehicle-type entries into Redis", rows.size());
    }
}
