package com.fasttag.dto;

import javax.validation.constraints.NotBlank;

public class AuthenticationRequest {
    @NotBlank
    private String user_name;
    @NotBlank
    private String password;
    @NotBlank
    private String Tollplaza_name;

    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTollplaza_name() { return Tollplaza_name; }
    public void setTollplaza_name(String tollplaza_name) { Tollplaza_name = tollplaza_name; }
}
