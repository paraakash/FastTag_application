package com.fasttag.controller;

import com.fasttag.dto.BalanceRequest;
import com.fasttag.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class BalanceController {

    private final BalanceService service;

    @Autowired
    public BalanceController(BalanceService service) { this.service = service; }

    @PostMapping("/update_balance")
    public String updateBalance(@RequestBody BalanceRequest req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return service.updateBalance(req, ip);
    }
}
