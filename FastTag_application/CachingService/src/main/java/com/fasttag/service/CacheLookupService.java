package com.fasttag.service;

import com.fasttag.dao.CacheLookupDao;
import org.springframework.stereotype.Service;

@Service
public class CacheLookupService {

    private final CacheLookupDao cacheLookupDao;

    public CacheLookupService(CacheLookupDao cacheLookupDao) {
        this.cacheLookupDao = cacheLookupDao;
    }

    public String getTollAmount(String tollplazaName, String vehicleType) {
        validateRequired("Tollplaza_name", tollplazaName);
        validateRequired("vehicle_type", vehicleType);
        return cacheLookupDao.getTollAmount(tollplazaName, vehicleType);
    }

    public String getVehicleType(String vehicleRegistrationNumber) {
        validateRequired("Vehicle_registration_nbr", vehicleRegistrationNumber);
        return cacheLookupDao.getVehicleType(vehicleRegistrationNumber);
    }

    private void validateRequired(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
