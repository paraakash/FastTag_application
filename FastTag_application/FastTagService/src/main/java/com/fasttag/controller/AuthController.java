package com.fasttag.controller;

import com.fasttag.dto.AuthenticationRequest;
import com.fasttag.dto.AuthenticationResponse;
import com.fasttag.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class AuthController {

    private final AuthService service;

    @Autowired
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/user_authentcation")
    public AuthenticationResponse authenticate(@javax.validation.Valid @RequestBody AuthenticationRequest req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return service.authenticate(req, ip);
    }
}
