package com.fasttag.dto;

public class BalanceRequest {
    private String token_string;
    private String user_name;
    private String vehicle_registration_number;
    private String fast_tag;
    private String Tollplaza_name;
    private Boolean primary_camera;

    public String getToken_string() { return token_string; }
    public void setToken_string(String token_string) { this.token_string = token_string; }
    public String getUser_name() { return user_name; }
    public void setUser_name(String user_name) { this.user_name = user_name; }
    public String getVehicle_registration_number() { return vehicle_registration_number; }
    public void setVehicle_registration_number(String vehicle_registration_number) { this.vehicle_registration_number = vehicle_registration_number; }
    public String getFast_tag() { return fast_tag; }
    public void setFast_tag(String fast_tag) { this.fast_tag = fast_tag; }
    public String getTollplaza_name() { return Tollplaza_name; }
    public void setTollplaza_name(String tollplaza_name) { Tollplaza_name = tollplaza_name; }
    public Boolean getPrimary_camera() { return primary_camera; }
    public void setPrimary_camera(Boolean primary_camera) { this.primary_camera = primary_camera; }
}
