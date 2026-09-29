package com.fasttag.service;

import com.fasttag.dto.BalanceRequest;
import com.fasttag.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BalanceService {

    private final Logger log = LoggerFactory.getLogger(BalanceService.class);
    private final RedisTemplate<String, String> redis;
    private final TransactionRepository txRepo;
    private final DataSource dataSource;

    @Autowired
    public BalanceService(RedisTemplate<String, String> redis, TransactionRepository txRepo, DataSource dataSource) {
        this.redis = redis;
        this.txRepo = txRepo;
        this.dataSource = dataSource;
    }

    public com.fasttag.dto.BalanceResponse updateBalance(BalanceRequest req, String ipAddress) {
        String cacheKey = req.getVehicle_registration_number() + "|" + req.getFast_tag() + "|" + req.getTollplaza_name();

        try {
            if (Boolean.FALSE.equals(req.getPrimary_camera())) {
                // 1: validate token and user via redis
                String tokenVal = redis.opsForValue().get(req.getToken_string());
                if (tokenVal == null || !tokenVal.startsWith(req.getUser_name() + "|" + req.getTollplaza_name())) {
                    throw new com.fasttag.exception.ServiceException(1001, "Invalid token");
                }

                // 3: check cache validation flag
                String valFlag = redis.opsForValue().get(cacheKey);
                if ("VALID_OK".equals(valFlag)) {
                    return new com.fasttag.dto.BalanceResponse(true, "Validation OK");
                }

                String vehicleType = redis.opsForValue().get("vehicle:" + req.getVehicle_registration_number());
                if (vehicleType == null || vehicleType.isBlank()) {
                    throw new com.fasttag.exception.ServiceException(1002, "Vehicle type not found in Redis cache");
                }

                String tollKey = "toll:" + req.getTollplaza_name() + ":" + vehicleType;
                String tollAmountText = redis.opsForValue().get(tollKey);
                if (tollAmountText == null || tollAmountText.isBlank()) {
                    throw new com.fasttag.exception.ServiceException(1003, "Toll amount not found in Redis cache");
                }

                java.math.BigDecimal tollAmount = new java.math.BigDecimal(tollAmountText.trim());

                // 4: check DB and set cache
                java.math.BigDecimal currentBalance = txRepo.existsInBalance(req.getVehicle_registration_number(), req.getFast_tag(), req.getTollplaza_name());
                if (currentBalance == null) {
                    throw new com.fasttag.exception.ServiceException(1004, "Vehicle/fast_tag not found");
                }

                if (currentBalance.subtract(tollAmount).compareTo(java.math.BigDecimal.ZERO) > 0) {
                    redis.opsForValue().set(cacheKey, "VALID_OK", 10, TimeUnit.MINUTES);
                    return new com.fasttag.dto.BalanceResponse(true, "Validation OK");
                } else {
						throw new com.fasttag.exception.ServiceException(1005, "Insufficient balance");
				}
            } else {
                // primary_camera == true
                String vehicleType = redis.opsForValue().get("vehicle:" + req.getVehicle_registration_number());
                if (vehicleType == null || vehicleType.isBlank()) {
                    throw new com.fasttag.exception.ServiceException(2002, "Vehicle type not found in Redis cache");
                }

                String tollKey = "toll:" + req.getTollplaza_name() + ":" + vehicleType;
                String tollAmountText = redis.opsForValue().get(tollKey);
                if (tollAmountText == null || tollAmountText.isBlank()) {
                    throw new com.fasttag.exception.ServiceException(2003, "Toll amount not found in Redis cache");
                }

                java.math.BigDecimal tollAmount = new java.math.BigDecimal(tollAmountText.trim());
                java.math.BigDecimal currentBalance = txRepo.existsInBalance(req.getVehicle_registration_number(), req.getFast_tag(), req.getTollplaza_name());
                if (currentBalance == null) {
                    throw new com.fasttag.exception.ServiceException(2004, "Vehicle/fast_tag not found");
                }

                if (currentBalance.subtract(tollAmount).compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    throw new com.fasttag.exception.ServiceException(2005, "Insufficient balance");
                }

                String valFlag = redis.opsForValue().get(cacheKey);
                // even if not present, proceed to call DB procedure (caller requested)
                java.sql.Connection conn = null;
                boolean previousAutoCommit = true;
                try {
                    conn = dataSource.getConnection();
                    previousAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                    java.sql.CallableStatement cs = conn.prepareCall("{call do_balance_update(?,?,?,?,?,?,?,?)}");
                    cs.setString(1, req.getVehicle_registration_number());
                    cs.setString(2, req.getFast_tag());
                    cs.setString(3, ipAddress);
                    cs.setString(4, req.getTollplaza_name());
                    cs.setBigDecimal(5, tollAmount);
                    cs.setBigDecimal(6, currentBalance);
                    cs.setString(7, vehicleType);
                    cs.registerOutParameter(8, java.sql.Types.VARCHAR);
                    cs.registerOutParameter(9, java.sql.Types.NUMERIC);
                    cs.execute();
                    String retMsg = cs.getString(8);
                    int retCode = cs.getInt(9);
                    if (retCode == 1) {
                        conn.commit();
                        // update cache on success
                        redis.opsForValue().set(cacheKey, "VALID_OK", 10, TimeUnit.MINUTES);
                        return new com.fasttag.dto.BalanceResponse(true, "Success: " + retMsg);
                    } else if (retCode == -2) {
                        // no rollback here: the procedure intentionally rejects insufficient balance as a business condition
                        throw new com.fasttag.exception.ServiceException(2006, "Insufficient balance");
                    } else {
                        conn.rollback();
                        throw new com.fasttag.exception.ServiceException(2001, "Procedure failed: " + retMsg);
                    }
                } catch (Exception ex) {
                    if (conn != null) {
                        try { conn.rollback(); } catch (Exception e) { log.warn("rollback failed", e); }
                    }
                    throw ex;
                } finally {
                    if (conn != null) {
                        try { conn.setAutoCommit(previousAutoCommit); conn.close(); } catch (Exception e) { log.warn("cleanup failed", e); }
                    }
                }
            }
        } catch (com.fasttag.exception.ServiceException se) {
            log.warn("Business error", se);
            throw se;
        } catch (Exception ex) {
            log.error("Error in updateBalance", ex);
            throw new com.fasttag.exception.ServiceException(5000, ex.getMessage());
        }
    }
}
