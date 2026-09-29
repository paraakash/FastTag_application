package com.fasttag.service;

import com.fasttag.dto.AuthenticationRequest;
import com.fasttag.dto.AuthenticationResponse;
import com.fasttag.repository.AuthRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasttag.util.JwtUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private final AuthRepository repo;
    private final RedisTemplate<String, String> redis;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(AuthRepository repo, RedisTemplate<String, String> redis, JwtUtil jwtUtil) {
        this.repo = repo;
        this.redis = redis;
        this.jwtUtil = jwtUtil;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest req, String ipAddress) {
        String encPwd = repo.findEncryptedPassword(req.getTollplaza_name(), req.getUser_name());
        boolean ok = false;
        if (encPwd != null) {
            // assume stored encrypted_password is BCrypt hash
            ok = BCrypt.checkpw(req.getPassword(), encPwd);
        }

        long authLogId = System.currentTimeMillis();
        Timestamp now = Timestamp.from(Instant.now());
        repo.insertAuthLog(authLogId, req.getTollplaza_name(), req.getUser_name(), now, ok ? "SUCCESS" : "FAILURE", ipAddress);

        if (!ok) {
            return new AuthenticationResponse(false, null, "Invalid credentials");
        }

        long ttl = 24 * 3600L;
        String token = jwtUtil.generateToken(req.getUser_name(), ttl);
        Timestamp expiry = Timestamp.from(Instant.now().plusSeconds(ttl));
        repo.updateToken(req.getTollplaza_name(), req.getUser_name(), token, expiry);
        // store token in redis with expiry, value contains username|Tollplaza_name for quick auth
        String redisVal = req.getUser_name() + "|" + req.getTollplaza_name();
        redis.opsForValue().set(token, redisVal);
        redis.expire(token, ttl, TimeUnit.SECONDS);

        return new AuthenticationResponse(true, token, "Authenticated");
    }
}
