package com.fasttag.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbc;

    @Autowired
    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String findEncryptedPassword(String tollplaza, String username) {
        String sql = "SELECT encrypted_password FROM service_authentication WHERE Tollplaza_name = ? AND UserName = ?";
        return jdbc.query(sql, new Object[]{tollplaza, username}, rs -> rs.next() ? rs.getString(1) : null);
    }

    public void insertAuthLog(long authLogId, String tollplaza, String username, Timestamp loginTime, String status, String ip) {
        String sql = "INSERT INTO authentication_log (auth_log_id, Tollplaza_name, UserName, Login_date_time, authentication_status, ip_address) VALUES (?,?,?,?,?,?)";
        jdbc.update(sql, authLogId, tollplaza, username, loginTime, status, ip);
    }

    public void updateToken(String tollplaza, String username, String token, Timestamp expiry) {
        String sql = "UPDATE service_authentication SET token_string = ?, token_expiry = ? WHERE Tollplaza_name = ? AND UserName = ?";
        jdbc.update(sql, token, expiry, tollplaza, username);
    }
}
