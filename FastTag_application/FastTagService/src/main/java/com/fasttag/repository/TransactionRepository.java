package com.fasttag.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public TransactionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public java.math.BigDecimal existsInBalance(String vehReg, String fastTag, String tollplaza) {
        String sql = "SELECT currnet_bal_amt FROM transaction_balance WHERE Vehocle_registration_nbr = ? AND fast_tag_id = ? AND ROWNUM = 1";
        try {
            return jdbc.queryForObject(sql, new Object[]{vehReg, fastTag}, java.math.BigDecimal.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public void insertTransactionLog(String txnId, String vehReg, String fastTag, String vehicleType, double currBal, double deducted, double newBal, java.sql.Timestamp createdDt) {
        String sql = "INSERT INTO transaction_log (txn_id, Vehocle_registration_nbr, fast_tag_id, vehicle_type, currnet_bal_amt, deducted_amt, new_bal_amt, ceated_dt) VALUES (?,?,?,?,?,?,?,?)";
        jdbc.update(sql, txnId, vehReg, fastTag, vehicleType, currBal, deducted, newBal, createdDt);
    }
}
