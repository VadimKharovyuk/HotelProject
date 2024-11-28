package com.example.hotelproject.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class GeoCode {
    @JsonAlias("latitude")
    private Double lat;

    @JsonAlias("longitude")
    private Double lon;
}