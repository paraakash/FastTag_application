package com.fasttag.controller;

import com.fasttag.service.CacheLookupService;
import com.fasttag.service.RedisCacheLoaderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class RedisCacheController {

    private final RedisCacheLoaderService cacheLoader;
    private final CacheLookupService cacheLookupService;

    public RedisCacheController(RedisCacheLoaderService cacheLoader, CacheLookupService cacheLookupService) {
        this.cacheLoader = cacheLoader;
        this.cacheLookupService = cacheLookupService;
    }

    @GetMapping("/toll")
    public ResponseEntity<Object> getTollAmount(
            @RequestParam String Tollplaza_name,
            @RequestParam String vehicle_type) {
        try {
            String value = cacheLookupService.getTollAmount(Tollplaza_name, vehicle_type);
            return ResponseEntity.ok(value != null ? value : "NOT_FOUND");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/vehicle")
    public ResponseEntity<Object> getVehicleType(@RequestParam String Vehicle_registration_nbr) {
        try {
            String value = cacheLookupService.getVehicleType(Vehicle_registration_nbr);
            return ResponseEntity.ok(value != null ? value : "NOT_FOUND");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refreshCache() {
        cacheLoader.loadFullTollMasterCache();
        cacheLoader.loadFullVehicleBalanceCache();
        return ResponseEntity.ok("Cache refreshed");
    }
}
