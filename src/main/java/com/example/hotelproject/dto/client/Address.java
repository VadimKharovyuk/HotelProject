package com.example.hotelproject.dto.client;

import lombok.Data;

@Data
public class Address {
    private String street;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
}