package com.fasttag.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CacheLookupDao {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.toll.key-prefix:toll}")
    private String tollKeyPrefix;

    @Value("${cache.vehicle.key-prefix:vehicle}")
    private String vehicleKeyPrefix;

    public CacheLookupDao(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getTollAmount(String tollplazaName, String vehicleType) {
        String key = tollKeyPrefix + ":" + tollplazaName + ":" + vehicleType;
        return redisTemplate.opsForValue().get(key);
    }

    public String getVehicleType(String vehicleRegistrationNumber) {
        String key = vehicleKeyPrefix + ":" + vehicleRegistrationNumber;
        return redisTemplate.opsForValue().get(key);
    }
}
